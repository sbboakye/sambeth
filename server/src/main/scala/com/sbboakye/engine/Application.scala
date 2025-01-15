package com.sbboakye.engine

import cats.*
import cats.effect.*
import com.sbboakye.engine.config.{AppConfig, Database}
import pureconfig.ConfigSource
import com.sbboakye.engine.config.syntax.*
import com.sbboakye.engine.contexts.RepositoryContext
import com.sbboakye.engine.routes.ScheduleRoutes
import com.sbboakye.engine.services.ScheduleService
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Application extends IOApp.Simple:

  given logger: Logger[IO]                     = Slf4jLogger.getLogger[IO]
  val repositoryContext: RepositoryContext[IO] = RepositoryContext[IO]
  import repositoryContext.schedulesRepositorySetup

  private val makeAPIServer: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(emberConfig, dbConfig) =>
      val apiResource = for {
        xa              <- Database.makeDbResource[IO](dbConfig)
        scheduleService <- ScheduleService[IO](xa)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(ScheduleRoutes(scheduleService).routes.orNotFound)
          .build
      } yield server
      apiResource.use(_ => IO.println("Server has started...") *> IO.never)
    }

  override def run: IO[Unit] = makeAPIServer
