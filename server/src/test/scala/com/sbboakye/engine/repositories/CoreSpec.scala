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
import io.circe.Json
import org.http4s.{EntityDecoder, HttpRoutes, Method, Request, Response, Status, Uri}
import org.http4s.circe.*
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

  def extractRelevantFields(json: Json, fieldsToExtract: List[String]): Json = {
    json.hcursor
      .downField("data")
      .withFocus { dataArray =>
        dataArray.mapArray { array =>
          array.map { obj =>
            Json.obj(
              fieldsToExtract.flatMap { field =>
                obj.hcursor.downField(field).focus.map(value => field -> value)
              }*
            )
          }
        }
      }
      .top
      .getOrElse(Json.Null)
  }

  def check(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[Json],
      fieldsToCompare: List[String]
  )(using
      ev: EntityDecoder[IO, Json]
  ): IO[Boolean] =
    given Eq[IO[Vector[Byte]]] = Eq.fromUniversalEquals
    actual.attempt.flatMap {
      case Right(actualResponse) =>
        val statusCheck = actualResponse.status == expectedStatus
        val bodyCheckIO =
          expectedBody.fold[IO[Boolean]](IO.pure(actualResponse.body.compile.toVector.isEmpty)) {
            expectedJson =>
              println(s"expected: $expectedJson")
              actualResponse.as[Json].map { actualJson =>
                val actualRelevant   = extractRelevantFields(actualJson, fieldsToCompare)
                val expectedRelevant = extractRelevantFields(expectedJson, fieldsToCompare)
                println(s"actualResponse: $actualJson")
                actualRelevant == expectedRelevant
              }
          }
        bodyCheckIO.map(_ && statusCheck)
      case Left(_) => IO.pure(false)
    }

  def testAPIEndpoints(
      httpMethod: Method,
      httpStatus: Status,
      endpoint: String,
      expectedJson: Option[Json] = None,
      fieldsToCompare: List[String] = List.empty
  )(getRoutes: (Transactor[IO]) => IO[HttpRoutes[IO]]): IO[Boolean] =
    coreSpecTransactor.use { xa =>
      val router = getRoutes(xa)
      router.flatMap { routes =>
        val request  = Request[IO](httpMethod, Uri.unsafeFromString(endpoint))
        val response = routes.run(request).value.map(_.get)
        check(response, httpStatus, expectedJson, fieldsToCompare)
      }
    }
