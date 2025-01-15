package com.sbboakye.engine.contexts

import cats.effect.*
import com.sbboakye.engine.contexts.RepositoryContext.NoHelper
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

class RepositoryContext[F[_]: MonadCancelThrow: Logger]:

  given connectorsRepositorySetup: RepositorySetup[ConnectorsRepository, NoHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (ConnectorsRepository[F], Unit, Transactor[F]) => F[T]
    ): F[T] =
      import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given
      given Core[F, Connector] with {}
      given Transactor[F] = xa
      ConnectorsRepository[F].use { repo =>
        run(repo, (), xa)
      }

  given pipelineExecutionLogsRepositorySetup
      : RepositorySetup[PipelineExecutionLogsRepository, NoHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (PipelineExecutionLogsRepository[F], Unit, Transactor[F]) => F[T]
    ): F[T] =
      given Core[F, PipelineExecutionLog] with {}
      given Transactor[F] = xa
      PipelineExecutionLogsRepository[F].use { repo =>
        run(repo, (), xa)
      }

  given schedulesRepositorySetup: RepositorySetup[SchedulesRepository, NoHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (SchedulesRepository[F], Unit, Transactor[F]) => F[T]
    ): F[T] =
      given Core[F, Schedule] with {}
      given Transactor[F] = xa
      SchedulesRepository[F].use { repo =>
        run(repo, (), xa)
      }

  given pipelinesRepositorySetup: RepositorySetup[PipelinesRepository, StagesHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (PipelinesRepository[F], StagesHelper[F], Transactor[F]) => F[T]
    ): F[T] =
      given Core[F, Pipeline] with  {}
      given Core[F, Stage] with     {}
      given Core[F, Connector] with {}
      given Transactor[F] = xa
      ConnectorsRepository[F].use { cRepo =>
        given ConnectorsRepository[F] = cRepo
        StagesRepository[F].use { sRepo =>
          given StagesRepository[F]         = sRepo
          val stagesHelper: StagesHelper[F] = StagesHelper[F]
          PipelinesRepository[F].use { repo =>
            run(repo, stagesHelper, xa)
          }
        }
      }

  given stagesRepositorySetup: RepositorySetup[StagesRepository, ConnectorsHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (StagesRepository[F], ConnectorsHelper[F], Transactor[F]) => F[T]
    ): F[T] =
      given Core[F, Stage] with     {}
      given Core[F, Connector] with {}
      given Transactor[F] = xa
      ConnectorsRepository[F].use { cRepo =>
        given ConnectorsRepository[F]             = cRepo
        val connectorsHelper: ConnectorsHelper[F] = ConnectorsHelper[F]
        StagesRepository[F].use { repo =>
          run(repo, connectorsHelper, xa)
        }
      }

  given pipelineExecutionsRepositorySetup
      : RepositorySetup[PipelineExecutionsRepository, PipelineExecutionLogsHelper, F] with
    def use[T](xa: Transactor[F])(
        run: (
            PipelineExecutionsRepository[F],
            PipelineExecutionLogsHelper[F],
            Transactor[F]
        ) => F[T]
    ): F[T] =
      given Core[F, PipelineExecutionLog] with {}
      given Core[F, PipelineExecution] with    {}
      given Transactor[F] = xa
      PipelineExecutionLogsRepository[F].use { cRepo =>
        given PipelineExecutionLogsRepository[F]   = cRepo
        val helper: PipelineExecutionLogsHelper[F] = PipelineExecutionLogsHelper[F]
        PipelineExecutionsRepository[F].use { repo =>
          run(repo, helper, xa)
        }
      }

object RepositoryContext:
  type NoHelper[F[_]] = Unit

  def apply[F[_]: MonadCancelThrow: Logger]: RepositoryContext[F] = new RepositoryContext[F]
