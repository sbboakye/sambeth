package com.sbboakye.engine.repositories.stage

import cats.*
import cats.instances.seq.*
import cats.effect.*
import cats.syntax.all.*
import cats.syntax.parallel.*
import com.sbboakye.engine.domain.CustomTypes.StageId
import com.sbboakye.engine.domain.{Connector, Stage}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class StagesRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
    xa: Transactor[F],
    core: Core[F, Stage]
):

//  private def enrichStages(fetchStages: => F[Seq[StageConnectorJoined]]): F[Seq[Stage]] =
//    fetchStages.flatMap { rows =>
//      // Group rows by stageId
//      val grouped = rows.groupBy(_.stageId)
//
//      grouped.toSeq.parTraverse { case (stageId, stageRows) =>
//        val firstRow = stageRows.head
//
//        // Extract connectors for the stage
//        val connectors = stageRows.flatMap { row =>
//          row.connectorId.map { _ =>
//            Connector(
//              id = row.connectorId.get,
//              stageId = row.connectorStageId.get,
//              name = row.connectorName.get,
//              connectorType = row.connectorType.get,
//              configuration = row.connectorConfiguration.get,
//              createdAt = row.connectorCreatedAt.get,
//              updatedAt = row.connectorUpdatedAt.get
//            )
//          }
//        }
//
//        // Create the Stage object
//        MonadCancelThrow[F].pure(
//          Stage(
//            id = firstRow.stageId,
//            pipelineID = firstRow.pipelineId,
//            stageType = firstRow.stageType,
//            connectors = connectors,
//            configuration = firstRow.stageConfiguration,
//            position = firstRow.stagePosition,
//            createdAt = firstRow.stageCreatedAt,
//            updatedAt = firstRow.stageUpdatedAt
//          )
//        )
//      }
//    }

  def findAll(
      offset: Int,
      limit: Int
  )(using
      connectorRepository: ConnectorsRepository[F]
  ): F[Seq[Stage]] =
    val results = for {
      stages   <- core.findAll(StageQueries.select, offset, limit, StageQueries.limitAndOffset)
      stageIds <- stages.map(_.id).pure[F]
      connectorsStages <- Stage.loadConnectors(stageIds.toList)
    } yield (stages, connectorsStages)

    results.map { case (stages, connectors) =>
      val connectorsByStageId = connectors.groupBy(_.stageId)
      stages.map(stage =>
        stage.copy(connectors = connectorsByStageId.getOrElse(stage.id, Seq.empty[Connector]))
      )
    }

//    fetchStagesWithConnectorsJoin(offset: Int, limit: Int)

//  private def fetchStagesWithConnectorsJoin(offset: Int, limit: Int): F[Seq[Stage]] =
//
//    def getStages: F[
//      Seq[StageConnectorJoined]
//    ] =
//      (StageQueries.select ++ StageQueries.limitAndOffset(offset, limit))
//        .query[StageConnectorJoined]
//        .to[Seq]
//        .transact(xa)

//    enrichStages(getStages)

  def findById(id: StageId)(using
      connectorRepository: ConnectorsRepository[F]
  ): F[Option[Stage]] =
//    fetchStageWithConnectors(id)
    val results = for {
      stage           <- core.findByID(StageQueries.select, StageQueries.where(id = id))
      stageId         <- stage.map(_.id).pure[F]
      connectorsStage <- Stage.loadConnectors(stageId.toList)
    } yield (stage, connectorsStage)

    results.map { case (stage, connectors) =>
      val connectorsByStageId = connectors.groupBy(_.stageId)
      stage.map(s => s.copy(connectors = connectorsByStageId.getOrElse(s.id, Seq.empty[Connector])))
    }

//  private def fetchStageWithConnectors(id: UUID): F[Option[Stage]] =
//
//    def getStage: F[
//      Option[StageConnectorJoined]
//    ] =
//      (StageQueries.select ++ StageQueries.where(column = "s.id", id = id))
//        .query[StageConnectorJoined]
//        .option
//        .transact(xa)
//
//    getStage.flatMap {
//      case Some(stage) => enrichStages(MonadCancelThrow[F].pure(Seq(stage))).map(_.headOption)
//      case None        => MonadCancelThrow[F].pure(None)
//    }

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
