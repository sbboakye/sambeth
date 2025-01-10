package com.sbboakye.engine.repositories.executionLog

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.CustomTypes.ExecutionId
import com.sbboakye.engine.domain.ExecutionLog
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class ExecutionLogsRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, ExecutionLog]
):

  def findAll(offset: Int, limit: Int): F[Seq[ExecutionLog]] =
    core.findAll(ExecutionLogQueries.select, offset, limit, ExecutionLogQueries.limitAndOffset)

  def findById(id: UUID): F[Option[ExecutionLog]] =
    core.findByID(ExecutionLogQueries.select, ExecutionLogQueries.where(id = id))

  def create(schedule: ExecutionLog): F[UUID] = core.create(ExecutionLogQueries.insert(schedule))

  def delete(id: UUID): F[Option[Int]] = core.delete(ExecutionLogQueries.delete(id))

  def findAllByExecutionIds(ids: List[ExecutionId]): F[Seq[ExecutionLog]] =
    (ExecutionLogQueries.select ++ ExecutionLogQueries.whereIn("execution_id", ids))
      .query[ExecutionLog]
      .to[Seq]
      .transact(xa)

object ExecutionLogsRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, ExecutionLog]
  ): Resource[F, ExecutionLogsRepository[F]] =
    Resource.eval(Async[F].pure(new ExecutionLogsRepository[F]))
