package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.PipelineExecution
import com.sbboakye.engine.repositories.execution.PipelineExecutionsRepository
import doobie.Transactor

class PipelineExecutionService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[PipelineExecutionsRepository, F]
) extends CoreService[F, PipelineExecutionsRepository, PipelineExecution](xa)

object PipelineExecutionService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[PipelineExecutionsRepository, F]
  ): Resource[F, PipelineExecutionService[F]] =
    Resource.eval(F.pure(new PipelineExecutionService[F](xa)))
