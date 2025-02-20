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
import com.sbboakye.engine.repositories.Repository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class PipelinesRepository[F[_]: { MonadCancelThrow, Logger }](using
    xa: Transactor[F],
    helper: StagesHelper[F]
) extends Core[F, Pipeline]
    with Repository[F, Pipeline]
    with PipelineMetadataRepository[F]:

  override def findMetadataById(id: PipelineId): F[Option[PipelineMetadata]] =
    (PipelineQueries.select ++ PipelineQueries.where(id = id))
      .query[PipelineMetadata]
      .option
      .transact(xa)

  override def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Pipeline]] =

    for {
      pipelines <- findAll(
        PipelineQueries.select,
        offset,
        limit,
        PipelineQueries.limitAndOffset
      )
      enrichPipelines <- helper.enrichPipelinesWithStages(pipelines)
    } yield enrichPipelines

  override def findById(id: UUID): F[Option[Pipeline]] =
    for {
      pipelineOpt <- findByID(PipelineQueries.select, PipelineQueries.where(id = id))
      enrichPipeline <- pipelineOpt match {
        case Some(pipeline) => helper.enrichPipelinesWithStages(Seq(pipeline)).map(_.headOption)
        case None           => MonadCancelThrow[F].pure(None)
      }
    } yield enrichPipeline

  override def create(pipeline: Pipeline): F[UUID] = create(PipelineQueries.insert(pipeline))

  override def update(id: UUID, pipeline: Pipeline): F[Option[Int]] =
    update(PipelineQueries.update(id, pipeline))

  override def delete(id: UUID): F[Option[Int]] = delete(PipelineQueries.delete(id))

object PipelinesRepository:
  def apply[F[_]: { MonadCancelThrow, Logger }](using
      Transactor[F],
      StagesHelper[F]
  ): Resource[F, PipelinesRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new PipelinesRepository[F]))
