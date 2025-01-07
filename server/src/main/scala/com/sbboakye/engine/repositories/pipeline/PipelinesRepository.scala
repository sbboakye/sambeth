package com.sbboakye.engine.repositories.pipeline

import cats.*
import cats.instances.seq.*
import cats.effect.*
import cats.syntax.all.*
import cats.syntax.parallel.*
import com.sbboakye.engine.domain.CustomTypes.PipelineScheduleStageConnectorJoined
import com.sbboakye.engine.domain.{
  Connector,
  ConnectorType,
  Pipeline,
  PipelineStatus,
  Schedule,
  Stage,
  StageType
}
import com.sbboakye.engine.repositories.core.Core
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
    core: Core[F, Pipeline]
):
  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

  private def enrichPipelines[A](
      fetchPipelines: => F[Seq[PipelineScheduleStageConnectorJoined]]
  ): F[Seq[Pipeline]] =
    fetchPipelines.flatMap(rows => {
      // Group rows by pipeline ID
      val grouped = rows.groupBy(_.pipelineId)

      // Traverse each pipeline and its associated rows
      grouped.toSeq.parTraverse { case (pipelineId, pipelineRows) =>
        // Extract pipeline and schedule details from the first row
        val firstRow = pipelineRows.head

        // Extract stages and their connectors
        val stages = pipelineRows.flatMap { row =>
          for {
            stageId                <- row.stageId
            stagePipelineId        <- row.stagePipelineId
            stageType              <- row.stageType
            stageConfiguration     <- row.stageConfiguration
            stagePosition          <- row.stagePosition
            stageCreatedAt         <- row.stageCreatedAt
            stageUpdatedAt         <- row.stageUpdatedAt
            connectorId            <- row.connectorId
            connectorStageId       <- row.connectorStageId
            connectorName          <- row.connectorName
            connectorType          <- row.connectorType
            connectorConfiguration <- row.connectorConfiguration
            connectorCreatedAt     <- row.connectorCreatedAt
            connectorUpdatedAt     <- row.connectorUpdatedAt
          } yield {
            // Create Connector
            val connector = Connector(
              id = connectorId,
              stageId = connectorStageId,
              name = connectorName,
              connectorType = connectorType,
              configuration = connectorConfiguration,
              createdAt = connectorCreatedAt,
              updatedAt = connectorUpdatedAt
            )

            // Create Stage with Connectors
            Stage(
              id = stageId,
              pipelineID = stagePipelineId,
              stageType = stageType,
              connectors = Seq(connector),
              configuration = stageConfiguration,
              position = stagePosition,
              createdAt = stageCreatedAt,
              updatedAt = stageUpdatedAt
            )
          }
        }

        // Create Schedule object if schedule fields are present
        val schedule = for {
          id             <- firstRow.scheduleId
          cronExpression <- firstRow.scheduleCronExpression
          timezone       <- firstRow.scheduleTimezone
          createdAt      <- firstRow.scheduleCreatedAt
          updatedAt      <- firstRow.scheduleUpdatedAt
        } yield Schedule(
          id = id,
          cronExpression = cronExpression,
          timezone = timezone,
          createdAt = createdAt,
          updatedAt = updatedAt
        )

        // Combine and return the enriched Pipeline
        MonadCancelThrow[F].pure(
          Pipeline(
            id = firstRow.pipelineId,
            name = firstRow.pipelineName,
            description = firstRow.pipelineDescription,
            stages = stages,
            schedule = schedule,
            status = firstRow.pipelineStatus,
            createdAt = firstRow.pipelineCreatedAt,
            updatedAt = firstRow.pipelineUpdatedAt
          )
        )
      }
    })

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Pipeline]] =
    fetchPipelinesWithConnectorsJoin(offset: Int, limit: Int)

  private def fetchPipelinesWithConnectorsJoin(offset: Int, limit: Int): F[Seq[Pipeline]] =

    def getPipelines: F[
      Seq[PipelineScheduleStageConnectorJoined]
    ] =
      (PipelineQueries.select ++ PipelineQueries.limitAndOffset(offset, limit))
        .query[PipelineScheduleStageConnectorJoined]
        .to[Seq]
        .transact(xa)

    enrichPipelines(getPipelines)

  def findById(id: UUID): F[Option[Pipeline]] =
    fetchPipelineWithConnectors(id)

  private def fetchPipelineWithConnectors(id: UUID): F[Option[Pipeline]] =

    def getPipeline: F[
      Option[PipelineScheduleStageConnectorJoined]
    ] =
      (PipelineQueries.select ++ PipelineQueries.where(column = "p.id", id = id))
        .query[PipelineScheduleStageConnectorJoined]
        .option
        .transact(xa)

    getPipeline.flatMap {
      case Some(pipeline) =>
        enrichPipelines(MonadCancelThrow[F].pure(Seq(pipeline))).map(_.headOption)
      case None => MonadCancelThrow[F].pure(None)
    }

  def create(pipeline: Pipeline): F[UUID] = core.create(PipelineQueries.insert(pipeline))

  def update(id: UUID, pipeline: Pipeline): F[Option[Int]] =
    core.update(PipelineQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineQueries.delete(id))

object PipelinesRepository:
  def apply[F[_]: Async: Logger: Parallel](using
      xa: Transactor[F],
      core: Core[F, Pipeline]
  ): Resource[F, PipelinesRepository[F]] =
    Resource.eval(Async[F].pure(new PipelinesRepository[F]))
