package com.sbboakye.engine.repositories.executionLog

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.CustomTypes.ExecutionId
import com.sbboakye.engine.domain.PipelineExecutionLog
import com.sbboakye.engine.repositories.Repository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class PipelineExecutionLogsRepository[F[_]: { MonadCancelThrow, Logger }] private (using
    xa: Transactor[F]
) extends Core[F, PipelineExecutionLog]
    with Repository[F, PipelineExecutionLog]:

  def findAll(offset: Int, limit: Int): F[Seq[PipelineExecutionLog]] =
    findAll(
      PipelineExecutionLogQueries.select,
      offset,
      limit,
      PipelineExecutionLogQueries.limitAndOffset
    )

  def findById(id: UUID): F[Option[PipelineExecutionLog]] =
    findByID(PipelineExecutionLogQueries.select, PipelineExecutionLogQueries.where(id = id))

  def create(schedule: PipelineExecutionLog): F[UUID] =
    create(PipelineExecutionLogQueries.insert(schedule))

  def delete(id: UUID): F[Option[Int]] = delete(PipelineExecutionLogQueries.delete(id))

  def findAllByExecutionIds(ids: List[ExecutionId]): F[Seq[PipelineExecutionLog]] =
    (PipelineExecutionLogQueries.select ++ PipelineExecutionLogQueries.whereIn("execution_id", ids))
      .query[PipelineExecutionLog]
      .to[Seq]
      .transact(xa)

object PipelineExecutionLogsRepository:
  def apply[F[_]: { MonadCancelThrow, Logger }](using
      Transactor[F]
  ): Resource[F, PipelineExecutionLogsRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new PipelineExecutionLogsRepository[F]))
