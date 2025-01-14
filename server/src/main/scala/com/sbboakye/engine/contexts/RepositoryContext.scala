package com.sbboakye.engine.contexts

import cats.effect.*
import com.sbboakye.engine.domain.{
  Connector,
  Pipeline,
  PipelineExecution,
  PipelineExecutionLog,
  Schedule,
  Stage
}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.execution.{
  PipelineExecutionLogsHelper,
  PipelineExecutionsRepository
}
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import com.sbboakye.engine.repositories.pipeline.{PipelinesRepository, StagesHelper}
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import com.sbboakye.engine.repositories.stage.{ConnectorsHelper, StagesRepository}
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object RepositoryContext:

  import com.sbboakye.engine.repositories.*

  type NoHelper[F[_]] = Unit

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  given connectorsRepositorySetup: RepositorySetup[ConnectorsRepository, NoHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (ConnectorsRepository[IO], Unit, Transactor[IO]) => IO[T]
    ): IO[T] =
      import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given
      given Core[IO, Connector] with {}

      given Transactor[IO] = xa

      ConnectorsRepository[IO].use { repo =>
        run(repo, (), xa)
      }

  given pipelineExecutionLogsRepositorySetup
      : RepositorySetup[PipelineExecutionLogsRepository, NoHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (PipelineExecutionLogsRepository[IO], Unit, Transactor[IO]) => IO[T]
    ): IO[T] =
      given Core[IO, PipelineExecutionLog] with {}
      given Transactor[IO] = xa
      PipelineExecutionLogsRepository[IO].use { repo =>
        run(repo, (), xa)
      }

  given schedulesRepositorySetup: RepositorySetup[SchedulesRepository, NoHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (SchedulesRepository[IO], Unit, Transactor[IO]) => IO[T]
    ): IO[T] =
      given Core[IO, Schedule] with {}
      given Transactor[IO] = xa
      SchedulesRepository[IO].use { repo =>
        run(repo, (), xa)
      }

  given pipelinesRepositorySetup: RepositorySetup[PipelinesRepository, StagesHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (PipelinesRepository[IO], StagesHelper[IO], Transactor[IO]) => IO[T]
    ): IO[T] =
      given Core[IO, Pipeline] with  {}
      given Core[IO, Stage] with     {}
      given Core[IO, Connector] with {}
      given Transactor[IO] = xa
      ConnectorsRepository[IO].use { cRepo =>
        given ConnectorsRepository[IO] = cRepo
        StagesRepository[IO].use { sRepo =>
          given StagesRepository[IO]         = sRepo
          val stagesHelper: StagesHelper[IO] = StagesHelper[IO]
          PipelinesRepository[IO].use { repo =>
            run(repo, stagesHelper, xa)
          }
        }
      }

  given stagesRepositorySetup: RepositorySetup[StagesRepository, ConnectorsHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (StagesRepository[IO], ConnectorsHelper[IO], Transactor[IO]) => IO[T]
    ): IO[T] =
      given Core[IO, Stage] with     {}
      given Core[IO, Connector] with {}
      given Transactor[IO] = xa
      ConnectorsRepository[IO].use { cRepo =>
        given ConnectorsRepository[IO]             = cRepo
        val connectorsHelper: ConnectorsHelper[IO] = ConnectorsHelper[IO]
        StagesRepository[IO].use { repo =>
          run(repo, connectorsHelper, xa)
        }
      }

  given pipelineExecutionsRepositorySetup
      : RepositorySetup[PipelineExecutionsRepository, PipelineExecutionLogsHelper, IO] with
    def use[T](xa: Transactor[IO])(
        run: (
            PipelineExecutionsRepository[IO],
            PipelineExecutionLogsHelper[IO],
            Transactor[IO]
        ) => IO[T]
    ): IO[T] =
      given Core[IO, PipelineExecutionLog] with {}
      given Core[IO, PipelineExecution] with    {}
      given Transactor[IO] = xa
      PipelineExecutionLogsRepository[IO].use { cRepo =>
        given PipelineExecutionLogsRepository[IO]   = cRepo
        val helper: PipelineExecutionLogsHelper[IO] = PipelineExecutionLogsHelper[IO]
        PipelineExecutionsRepository[IO].use { repo =>
          run(repo, helper, xa)
        }
      }
