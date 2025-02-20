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

class RepositoryContext[F[_]: { MonadCancelThrow, Logger }]:

  given connectorsRepositorySetup: RepositorySetup[ConnectorsRepository, F] with
    def use[T](xa: Transactor[F])(
        run: (ConnectorsRepository[F], Transactor[F]) => F[T]
    ): F[T] =
      import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given
      given Transactor[F] = xa
      ConnectorsRepository[F].use { repo =>
        run(repo, xa)
      }

  given pipelineExecutionLogsRepositorySetup: RepositorySetup[PipelineExecutionLogsRepository, F]
  with
    def use[T](xa: Transactor[F])(
        run: (PipelineExecutionLogsRepository[F], Transactor[F]) => F[T]
    ): F[T] =
      given Transactor[F] = xa
      PipelineExecutionLogsRepository[F].use { repo =>
        run(repo, xa)
      }

  given schedulesRepositorySetup: RepositorySetup[SchedulesRepository, F] with
    def use[T](xa: Transactor[F])(
        run: (SchedulesRepository[F], Transactor[F]) => F[T]
    ): F[T] =
      given Transactor[F] = xa
      SchedulesRepository[F].use { repo =>
        run(repo, xa)
      }

  given pipelinesRepositorySetup: RepositorySetup[PipelinesRepository, F] with
    def use[T](xa: Transactor[F])(
        run: (PipelinesRepository[F], Transactor[F]) => F[T]
    ): F[T] =
      given Transactor[F] = xa
      ConnectorsRepository[F].use { cRepo =>
        given ConnectorsRepository[F] = cRepo
        given ConnectorsHelper[F]     = ConnectorsHelper[F]
        StagesRepository[F].use { sRepo =>
          given StagesRepository[F] = sRepo
          given StagesHelper[F]     = StagesHelper[F]
          PipelinesRepository[F].use { repo =>
            run(repo, xa)
          }
        }
      }

  given stagesRepositorySetup: RepositorySetup[StagesRepository, F] with
    def use[T](xa: Transactor[F])(
        run: (StagesRepository[F], Transactor[F]) => F[T]
    ): F[T] =
      given Transactor[F] = xa
      ConnectorsRepository[F].use { cRepo =>
        given ConnectorsRepository[F] = cRepo
        given ConnectorsHelper[F]     = ConnectorsHelper[F]
        StagesRepository[F].use { repo =>
          run(repo, xa)
        }
      }

  given pipelineExecutionsRepositorySetup: RepositorySetup[PipelineExecutionsRepository, F] with
    def use[T](xa: Transactor[F])(
        run: (
            PipelineExecutionsRepository[F],
            Transactor[F]
        ) => F[T]
    ): F[T] =
      given Transactor[F] = xa
      PipelineExecutionLogsRepository[F].use { cRepo =>
        given PipelineExecutionLogsRepository[F] = cRepo
        given PipelineExecutionLogsHelper[F]     = PipelineExecutionLogsHelper[F]
        PipelineExecutionsRepository[F].use { repo =>
          run(repo, xa)
        }
      }

object RepositoryContext:
  type NoHelper[F[_]] = Unit

  def apply[F[_]: { MonadCancelThrow, Logger }]: RepositoryContext[F] = new RepositoryContext[F]
