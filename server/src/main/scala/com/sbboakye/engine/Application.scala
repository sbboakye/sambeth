package com.sbboakye.engine

import cats.*
import cats.effect.*
import com.sbboakye.engine.config.{AppConfig, Database}
import pureconfig.ConfigSource
import com.sbboakye.engine.config.syntax.*
import com.sbboakye.engine.contexts.RepositoryContext
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.domain.ConnectorType.API
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.routes.ScheduleRoutes
import com.sbboakye.engine.services.{ConnectorService, ScheduleService}
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.OffsetDateTime
import java.util.UUID

object Application extends IOApp.Simple:

  given logger: Logger[IO]                     = Slf4jLogger.getLogger[IO]
  val repositoryContext: RepositoryContext[IO] = RepositoryContext[IO]
  import repositoryContext.schedulesRepositorySetup
  import repositoryContext.connectorsRepositorySetup

  private val makeAPIServer: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(emberConfig, dbConfig) =>
      val apiResource = for {
        xa      <- Database.makeDbResource[IO](dbConfig)
        sch     <- ScheduleService[IO](xa)
        service <- ConnectorService[IO](xa)
        sam <- Resource.eval(
          service.findById(UUID.fromString("33333333-3333-3333-3333-333333333331"))
        )
//        server <- EmberServerBuilder
//          .default[IO]
//          .withHost(emberConfig.host)
//          .withPort(emberConfig.port)
//          .withHttpApp(ScheduleRoutes(sch).routes.orNotFound)
//          .build
      } yield sam
      apiResource.use(_ => IO.println("Server has started...") *> IO.never)
    }

  override def run: IO[Unit] = makeAPIServer
