package com.sbboakye.engine

import cats.*
import cats.effect.*
import cats.implicits.*
import com.sbboakye.engine.config.{AppConfig, Database}
import pureconfig.ConfigSource
import com.sbboakye.engine.config.syntax.*
import com.sbboakye.engine.domain.CustomTypes.StageId
import com.sbboakye.engine.domain.{
  Connector,
  ConnectorType,
  PipelineExecution,
  PipelineExecutionLog,
  Pipeline,
  Stage
}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.execution.PipelineExecutionsRepository
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.*
import doobie.implicits.*
//import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.OffsetDateTime

object Application extends IOApp.Simple:

  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
//  given core2: Core[IO, Stage] with                {}
//  given core1: Core[IO, Connector] with            {}
//  given core3: Core[IO, Pipeline] with             {}
  given core4: Core[IO, PipelineExecutionLog] with {}
  given core5: Core[IO, PipelineExecution] with    {}

  val connector1: Connector = Connector(
    java.util.UUID.randomUUID(),
    java.util.UUID.randomUUID(),
    "test connector 1",
    ConnectorType.Database,
    Map("db_name" -> "postgresql"),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(dbConfig) =>
      val appResource =
        Database
          .makeDbResource[IO](dbConfig)
          .use { xa =>
            given transactor: Transactor[IO] = xa
            PipelineExecutionLogsRepository[IO].use { cRepo =>
              given PipelineExecutionLogsRepository[IO] = cRepo
              PipelineExecutionsRepository[IO].use { repo =>
                repo.findAll(0, 10)
              }
            }
//            ConnectorsRepository[IO].use { cRepo =>
//              given s: ConnectorsRepository[IO] = cRepo
//              StagesRepository[IO].use { repo =>
//                repo.findAll(0, 10)
//                //              repo.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
//              }
//            }
//            ConnectorsRepository[IO].use { repo =>
//              repo.findAll(0, 10)
////              repo.create(connector1)
//            }
//            PipelinesRepository[IO].use { repo =>
//              repo.findAll(0, 10)
//            }
          }
          .toResource
      appResource
        .use(conns => IO.println(conns) *> IO.println("Ending Now..."))
    }
