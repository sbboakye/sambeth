package com.sbboakye.engine.repositories.stage

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.sbboakye.engine.domain.CustomTypes.{PipelineId, StageId}
import com.sbboakye.engine.domain.{Connector, Stage}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*

import org.typelevel.log4cats.Logger

import java.util.UUID

class StagesRepository[F[_]: MonadCancelThrow: Logger](using
    xa: Transactor[F],
    core: Core[F, Stage]
):

  def findAll(
      offset: Int,
      limit: Int,
      helper: ConnectorsHelper[F]
  ): F[Seq[Stage]] =
    for {
      stages       <- core.findAll(StageQueries.select, offset, limit, StageQueries.limitAndOffset)
      enrichStages <- helper.enrichStagesWithConnectors(stages)
    } yield enrichStages

  def findById(id: StageId, helper: ConnectorsHelper[F]): F[Option[Stage]] =
    for {
      stageOpt <- core.findByID(StageQueries.select, StageQueries.where(id = id))
      enrichStage <- stageOpt match {
        case Some(stage) => helper.enrichStagesWithConnectors(Seq(stage)).map(_.headOption)
        case None        => MonadCancelThrow[F].pure(None)
      }
    } yield enrichStage

  def create(pipeline: Stage): F[UUID] = core.create(StageQueries.insert(pipeline))

  def update(id: UUID, pipeline: Stage): F[Option[Int]] =
    core.update(StageQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(StageQueries.delete(id))

  def findAllByPipelineIds(ids: List[PipelineId]): F[Seq[Stage]] =
    (StageQueries.select ++ StageQueries.whereIn("pipeline_id", ids))
      .query[Stage]
      .to[Seq]
      .transact(xa)

object StagesRepository:
  def apply[F[_]: MonadCancelThrow: Logger](using
      xa: Transactor[F],
      core: Core[F, Stage],
      connectorsRepository: ConnectorsRepository[F]
  ): Resource[F, StagesRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new StagesRepository[F]))
