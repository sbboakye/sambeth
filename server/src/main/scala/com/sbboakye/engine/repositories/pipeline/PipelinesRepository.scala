package com.sbboakye.engine.repositories.pipeline

import cats.*
import cats.instances.seq.*
import cats.effect.*
import cats.syntax.all.*
import cats.syntax.parallel.*
import com.sbboakye.engine.domain.CustomTypes.{PipelineId, PipelineScheduleStageConnectorJoined}
import com.sbboakye.engine.domain.{
  Connector,
  ConnectorType,
  Pipeline,
  PipelineMetadata,
  PipelineMetadataRepository,
  PipelineStatus,
  Schedule,
  Stage,
  StageType
}
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.*
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class PipelinesRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
    xa: Transactor[F],
    core: Core[F, Pipeline],
    stagesRepository: StagesRepository[F]
) extends PipelineMetadataRepository[F]:

  private def enrichPipelinesWithStages(
      pipelines: Seq[Pipeline],
      stages: Seq[Stage]
  ): Seq[Pipeline] =
    val pipelinesByStageId = stages.groupBy(_.pipelineId)
    pipelines.map(pipeline =>
      pipeline.copy(stages = pipelinesByStageId.getOrElse(pipeline.id, Seq.empty[Stage]))
    )

  override def findMetadataById(id: PipelineId): F[Option[PipelineMetadata]] =
    (PipelineQueries.select ++ PipelineQueries.where(id = id))
      .query[PipelineMetadata]
      .option
      .transact(xa)

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Pipeline]] =
    for {
      pipelines <- core.findAll(
        PipelineQueries.select,
        offset,
        limit,
        PipelineQueries.limitAndOffset
      )
      pipelineIds     <- pipelines.map(_.id).toList.pure[F]
      stagesPipelines <- Pipeline.loadStages(pipelineIds)
    } yield enrichPipelinesWithStages(pipelines, stagesPipelines)

  def findById(id: UUID): F[Option[Pipeline]] =
    for {
      pipelineOpt <- core.findByID(PipelineQueries.select, PipelineQueries.where(id = id))
      stages <- pipelineOpt match {
        case Some(pipeline) => Pipeline.loadStages(List(pipeline.id))
        case None           => MonadCancelThrow[F].pure(Seq.empty[Stage])
      }
    } yield pipelineOpt.map(pipeline => enrichPipelinesWithStages(Seq(pipeline), stages).head)

  def create(pipeline: Pipeline): F[UUID] = core.create(PipelineQueries.insert(pipeline))

  def update(id: UUID, pipeline: Pipeline): F[Option[Int]] =
    core.update(PipelineQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineQueries.delete(id))

object PipelinesRepository:
  def apply[F[_]: Async: Logger: Parallel](using
      xa: Transactor[F],
      core: Core[F, Pipeline],
      stagesRepository: StagesRepository[F]
  ): Resource[F, PipelinesRepository[F]] =
    Resource.eval(Async[F].pure(new PipelinesRepository[F]))
