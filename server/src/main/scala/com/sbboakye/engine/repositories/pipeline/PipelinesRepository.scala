package com.sbboakye.engine.repositories.pipeline

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Pipeline
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class PipelinesRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, Pipeline]
):

  def findAll(offset: Int, limit: Int): F[Seq[Pipeline]] =
    core.findAll(PipelineQueries.select, offset, limit, PipelineQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Pipeline]] =
    core.findByID(PipelineQueries.select, PipelineQueries.where(id = id))

  def create(pipeline: Pipeline): F[UUID] = core.create(PipelineQueries.insert(pipeline))

  def update(id: UUID, pipeline: Pipeline): F[Option[Int]] =
    core.update(PipelineQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineQueries.delete(id))

object PipelinesRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, Pipeline]
  ): Resource[F, PipelinesRepository[F]] =
    Resource.eval(Async[F].pure(new PipelinesRepository[F]))
