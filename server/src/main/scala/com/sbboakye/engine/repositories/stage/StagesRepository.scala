package com.sbboakye.engine.repositories.stage

import cats.*
import cats.instances.seq.*
import cats.effect.*
import cats.syntax.all.*
import cats.syntax.parallel.*
import com.sbboakye.engine.domain.CustomTypes.StageConnectorJoined
import com.sbboakye.engine.domain.{Connector, ConnectorType, Stage, StageType}
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class StagesRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
    xa: Transactor[F],
    core: Core[F, Stage]
):
  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

  private def enrichStages[A](
      fetchStages: => F[Seq[StageConnectorJoined]]
  ): F[Seq[Stage]] =
    fetchStages.flatMap(rows => {
      val grouped = rows.groupBy(_._1)
      grouped.toSeq.parTraverse { case (stageId, stageRows) =>
        val (
          stage_id,
          pipeline_id,
          stage_type,
          stage_configuration,
          stage_position,
          stage_created_at,
          stage_updated_at,
          _,
          _,
          _,
          _,
          _,
          _,
          _
        ) = stageRows.head
        val connectors = stageRows.flatMap {
          case (
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                Some(connector_id),
                Some(connector_stage_id),
                Some(connector_name),
                Some(connector_type),
                Some(connector_configuration),
                Some(connector_created_at),
                Some(connector_updated_at)
              ) =>
            Some(
              Connector(
                id = connector_id,
                stageId = connector_stage_id,
                name = connector_name,
                connectorType = connector_type,
                configuration = connector_configuration,
                createdAt = connector_created_at,
                updatedAt = connector_updated_at
              )
            )
          case _ => None
        }
        MonadCancelThrow[F].pure(
          Stage(
            id = stage_id,
            pipelineID = pipeline_id,
            stageType = stage_type,
            connectors = connectors,
            configuration = stage_configuration,
            position = stage_position,
            createdAt = stage_created_at,
            updatedAt = stage_updated_at
          )
        )
      }
    })

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Stage]] =
    fetchStagesWithConnectorsJoin(offset: Int, limit: Int)

  private def fetchStagesWithConnectorsJoin(offset: Int, limit: Int): F[Seq[Stage]] =

    def getStages: F[
      Seq[StageConnectorJoined]
    ] =
      (StageQueries.select ++ StageQueries.limitAndOffset(offset, limit))
        .query[StageConnectorJoined]
        .to[Seq]
        .transact(xa)

    enrichStages(getStages)

  def findById(id: UUID): F[Option[Stage]] =
    fetchStageWithConnectors(id)

  private def fetchStageWithConnectors(id: UUID): F[Option[Stage]] =

    def getStage: F[
      Option[StageConnectorJoined]
    ] =
      (StageQueries.select ++ StageQueries.where(column = "s.id", id = id))
        .query[StageConnectorJoined]
        .option
        .transact(xa)

    getStage.flatMap {
      case Some(stage) => enrichStages(MonadCancelThrow[F].pure(Seq(stage))).map(_.headOption)
      case None        => MonadCancelThrow[F].pure(None)
    }

  def create(pipeline: Stage): F[UUID] = core.create(StageQueries.insert(pipeline))

  def update(id: UUID, pipeline: Stage): F[Option[Int]] =
    core.update(StageQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(StageQueries.delete(id))

object StagesRepository:
  def apply[F[_]: Async: Logger: Parallel](using
      xa: Transactor[F],
      core: Core[F, Stage]
  ): Resource[F, StagesRepository[F]] =
    Resource.eval(Async[F].pure(new StagesRepository[F]))
