package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Pipeline
import com.sbboakye.engine.repositories.pipeline.{PipelinesRepository, StagesHelper}
import doobie.Transactor

import java.util.UUID

class PipelineService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[PipelinesRepository, F]
) extends CoreService[F, PipelinesRepository, Pipeline](xa)

object PipelineService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[PipelinesRepository, F]
  ): Resource[F, PipelineService[F]] =
    Resource.eval(F.pure(new PipelineService[F](xa)))
