package com.sbboakye.engine.repositories

import cats.*
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.{JdbcDatabaseContainer, PostgreSQLContainer}
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.testcontainers.utility.DockerImageName

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
