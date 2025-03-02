package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.PipelineExecutionLog
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import doobie.Transactor

class PipelineExecutionLogService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[PipelineExecutionLogsRepository, F]
) extends CoreService[F, PipelineExecutionLogsRepository, PipelineExecutionLog](xa)

object PipelineExecutionLogService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[PipelineExecutionLogsRepository, F]
  ): Resource[F, PipelineExecutionLogService[F]] =
    Resource.eval(F.pure(new PipelineExecutionLogService[F](xa)))
