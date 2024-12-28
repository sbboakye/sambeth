package com.sbboakye.engine.repositories.stage

import cats.*
import cats.instances.list.*
import cats.instances.seq.*
import cats.effect.*
import cats.syntax.all.*
import cats.syntax.parallel.*
import com.sbboakye.engine.domain.CustomTypes.{StageConfiguration, StageId}
import com.sbboakye.engine.domain.{Connector, Stage, StageType, StageWithConnectors}
import com.sbboakye.engine.repositories.connector.ConnectorQueries
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class StagesRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
    xa: Transactor[F],
    core: Core[F, (UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)]
):

  private def getConnectors(stageId: StageId): F[Seq[Connector]] =
    (ConnectorQueries.select ++ ConnectorQueries.where(column = "stage_id", id = stageId))
      .query[Connector]
      .to[Seq]
      .transact(xa)

  private def enrichStages[A](
      fetchStages: => F[Seq[A]],
      getConnectorsForStage: A => F[Seq[Connector]],
      convertToStage: (A, Seq[Connector]) => Stage
  ): F[Seq[Stage]] =
    fetchStages
      .map(_.parTraverse { stage =>
        getConnectorsForStage(stage).map { connectors =>
          convertToStage(stage, connectors)
        }
      })
      .flatten

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Stage]] =
    getAllStagesAfterJoin(offset: Int, limit: Int)

  private def getAllStagesAfterJoin(offset: Int, limit: Int): F[Seq[Stage]] =

    def getStages: F[
      Seq[(UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)]
    ] =
      (StageQueries.select ++ fr"LIMIT $limit OFFSET $offset")
        .query[(UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)]
        .to[Seq]
        .transact(xa)

    enrichStages(
      fetchStages = getStages,
      getConnectorsForStage = stage => getConnectors(stage._1),
      convertToStage = (stage, connectors) => StageWithConnectors(stage, connectors).convertToStage
    )

//    for {
//      allStages <- getStages
//      stagesWithConnectors <- allStages
//        .parTraverse { stage =>
//          val matchedConnectors = getConnectors(stage._1)
//          matchedConnectors.map(connectors => StageWithConnectors(stage, connectors).convertToStage)
//        }
//    } yield stagesWithConnectors

  def findById(id: UUID): F[Option[Stage]] =
    getStageAfterJoin(id)

  private def getStageAfterJoin(id: UUID): F[Option[Stage]] =

    def getStage: F[
      Option[(UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)]
    ] =
      (StageQueries.select ++ StageQueries.where(id = id))
        .query[(UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)]
        .option
        .transact(xa)

    getStage.flatMap {
      case Some(stage) =>
        enrichStages(
          fetchStages = Applicative[F].pure(Seq(stage)),
          getConnectorsForStage = stage => getConnectors(stage._1),
          convertToStage =
            (stage, connectors) => StageWithConnectors(stage, connectors).convertToStage
        ).map(_.headOption)
      case None => Applicative[F].pure(None)
    }

//    for {
//      stageOption <- getStage
//      stageWithConnector <- stageOption
//        .parTraverse { stage =>
//          val matchedConnectors = getConnectors(stage._1)
//          matchedConnectors.map(connectors => StageWithConnectors(stage, connectors).convertToStage)
//        }
//    } yield stageWithConnector

  def create(pipeline: Stage): F[UUID] = core.create(StageQueries.insert(pipeline))

  def update(id: UUID, pipeline: Stage): F[Option[Int]] =
    core.update(StageQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(StageQueries.delete(id))

object StagesRepository:
  def apply[F[_]: Async: Logger: Parallel](using
      xa: Transactor[F],
      core: Core[
        F,
        (UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime)
      ]
  ): Resource[F, StagesRepository[F]] =
    Resource.eval(Async[F].pure(new StagesRepository[F]))
