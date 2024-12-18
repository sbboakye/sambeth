package com.sbboakye.engine.repositories

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.{JdbcDatabaseContainer, PostgreSQLContainer}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
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
    ce <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.jdbcUrl,
      db.username,
      db.password,
      ce
    )
  } yield xa
