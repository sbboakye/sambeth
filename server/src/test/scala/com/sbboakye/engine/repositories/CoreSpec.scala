package com.sbboakye.engine.repositories

import cats.*
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import cats.kernel.Eq
import com.dimafeng.testcontainers.{JdbcDatabaseContainer, PostgreSQLContainer}
import com.sbboakye.engine.contexts.{RepositoryContext, RepositorySetup}
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.{EntityDecoder, Method, Request, Response, Status, Uri}
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CoreSpec:
  val initSqlString: String

  val postgres: Resource[IO, PostgreSQLContainer] = {
    val acquire = IO {
      val container = PostgreSQLContainer.Def(
        dockerImageName = DockerImageName.parse("postgres"),
        databaseName = "test-database-container",
        username = "scala",
        password = "scala",
        commonJdbcParams =
          JdbcDatabaseContainer.CommonParams(initScriptPath = Option(initSqlString))
      )
      container.start()
    }
    val release = (container: PostgreSQLContainer) => IO(container.close())
    Resource.make(acquire)(release)
  }

  val coreSpecTransactor: Resource[IO, Transactor[IO]] = for {
    db <- postgres
    ce <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.jdbcUrl,
      db.username,
      db.password,
      ce
    )
  } yield xa

  def executeSqlScript(scriptPath: String)(using xa: Transactor[IO]): IO[Unit] =
    val script     = scala.io.Source.fromResource(scriptPath).mkString
    val statements = script.split(";").filter(_.trim.nonEmpty)
    statements.toList.traverse_ { sql =>
      Fragment.const(sql.trim).update.run.transact(xa)
    }

  given logger: Logger[IO]                     = Slf4jLogger.getLogger[IO]
  val repositoryContext: RepositoryContext[IO] = RepositoryContext[IO]

  def withDependencies[Repo[_[_]], H[_[_]], T](
      test: (Repo[IO], H[IO], Transactor[IO]) => IO[T]
  )(using setup: RepositorySetup[Repo, H, IO]): IO[T] = {
    coreSpecTransactor.use { xa =>
      setup.use(xa) { (repo, helper, xa) =>
        test(repo, helper, xa)
      }
    }
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(using
      ev: EntityDecoder[IO, A]
  ): IO[Boolean] =
    given Eq[IO[Vector[Byte]]] = Eq.fromUniversalEquals
    actual.attempt.flatMap {
      case Right(actualResponse) =>
        val statusCheck = actualResponse.status == expectedStatus
        val bodyCheckIO =
          expectedBody.fold[IO[Boolean]](IO.pure(actualResponse.body.compile.toVector.isEmpty)) {
            expected =>
              actualResponse.as[A].map(_ == expected)
          }
        bodyCheckIO.map(_ && statusCheck)
      case Left(_) => IO.pure(false)
    }
