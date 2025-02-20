package com.sbboakye.engine.repositories.pipeline

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.sbboakye.engine.domain.CustomTypes.PipelineId
import com.sbboakye.engine.domain.{
  Pipeline,
  PipelineMetadata,
  PipelineMetadataRepository,
  PipelineStatus
}
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class PipelinesRepository[F[_]: { MonadCancelThrow, Logger }](using
    xa: Transactor[F],
    core: Core[F, Pipeline]
) extends PipelineMetadataRepository[F]:

  override def findMetadataById(id: PipelineId): F[Option[PipelineMetadata]] =
    (PipelineQueries.select ++ PipelineQueries.where(id = id))
      .query[PipelineMetadata]
      .option
      .transact(xa)

  def findAll(
      offset: Int,
      limit: Int,
      helper: StagesHelper[F]
  ): F[Seq[Pipeline]] =

    for {
      pipelines <- core.findAll(
        PipelineQueries.select,
        offset,
        limit,
        PipelineQueries.limitAndOffset
      )
      enrichPipelines <- helper.enrichPipelinesWithStages(pipelines)
    } yield enrichPipelines

  def findById(id: UUID, helper: StagesHelper[F]): F[Option[Pipeline]] =
    for {
      pipelineOpt <- core.findByID(PipelineQueries.select, PipelineQueries.where(id = id))
      enrichPipeline <- pipelineOpt match {
        case Some(pipeline) => helper.enrichPipelinesWithStages(Seq(pipeline)).map(_.headOption)
        case None           => MonadCancelThrow[F].pure(None)
      }
    } yield enrichPipeline

  def create(pipeline: Pipeline): F[UUID] = core.create(PipelineQueries.insert(pipeline))

  def update(id: UUID, pipeline: Pipeline): F[Option[Int]] =
    core.update(PipelineQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineQueries.delete(id))

object PipelinesRepository:
  def apply[F[_]: { MonadCancelThrow, Logger }](using
      xa: Transactor[F],
      core: Core[F, Pipeline]
  ): Resource[F, PipelinesRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new PipelinesRepository[F]))
