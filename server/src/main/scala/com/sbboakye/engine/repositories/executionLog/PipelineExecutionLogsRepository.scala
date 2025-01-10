package com.sbboakye.engine.repositories.executionLog

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.CustomTypes.ExecutionId
import com.sbboakye.engine.domain.PipelineExecutionLog
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class PipelineExecutionLogsRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, PipelineExecutionLog]
):

  def findAll(offset: Int, limit: Int): F[Seq[PipelineExecutionLog]] =
    core.findAll(
      PipelineExecutionLogQueries.select,
      offset,
      limit,
      PipelineExecutionLogQueries.limitAndOffset
    )

  def findById(id: UUID): F[Option[PipelineExecutionLog]] =
    core.findByID(PipelineExecutionLogQueries.select, PipelineExecutionLogQueries.where(id = id))

  def create(schedule: PipelineExecutionLog): F[UUID] =
    core.create(PipelineExecutionLogQueries.insert(schedule))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineExecutionLogQueries.delete(id))

  def findAllByExecutionIds(ids: List[ExecutionId]): F[Seq[PipelineExecutionLog]] =
    (PipelineExecutionLogQueries.select ++ PipelineExecutionLogQueries.whereIn("execution_id", ids))
      .query[PipelineExecutionLog]
      .to[Seq]
      .transact(xa)

object PipelineExecutionLogsRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, PipelineExecutionLog]
  ): Resource[F, PipelineExecutionLogsRepository[F]] =
    Resource.eval(Async[F].pure(new PipelineExecutionLogsRepository[F]))
