package com.sbboakye.engine

import cats.*
import cats.effect.*
import cats.implicits.*
import com.sbboakye.engine.config.{AppConfig, Database}
import pureconfig.ConfigSource
import com.sbboakye.engine.config.syntax.*
import com.sbboakye.engine.domain.CustomTypes.ConnectorConfiguration
import com.sbboakye.engine.domain.{Connector, Stage, StageType}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.generic.auto.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.OffsetDateTime
import java.util.UUID

object Application extends IOApp.Simple:

  import com.sbboakye.engine.repositories.core.ConnectorConfigurationJsonMeta.given

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
//  given core: Core[IO, Tuple7[UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime]] with {}
  given core: Core[IO, Connector] with {}

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(dbConfig) =>
      val appResource =
        Database
          .makeDbResource[IO](dbConfig)
          .use { xa =>
            given transactor: Transactor[IO] = xa
//          StagesRepository[IO].use { repo =>
//            repo.findAll(0, 10)
//          }
            ConnectorsRepository[IO].use { repo =>
              repo.findAll(0, 10)
            }
          }
          .toResource
      appResource
        .use(conns => IO.println(conns(0)) *> IO.println("Server has started...") *> IO.never)
    }
