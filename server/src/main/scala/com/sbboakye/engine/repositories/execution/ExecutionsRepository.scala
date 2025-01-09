package com.sbboakye.engine.repositories.execution

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.sbboakye.engine.domain.{Execution, ExecutionLog, ExecutionStatus}
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.executionLog.ExecutionLogsRepository
import doobie.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class ExecutionsRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
    xa: Transactor[F],
    core: Core[F, Execution],
    executionLogsRepository: ExecutionLogsRepository[F]
):
  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

  private def enrichExecutionsWithLogs(
      executions: Seq[Execution],
      logs: Seq[ExecutionLog]
  ): Seq[Execution] =
    val logsByExecutionId = logs.groupBy(_.executionId)
    executions.map(execution =>
      execution.copy(logs = logsByExecutionId.getOrElse(execution.id, Seq.empty[ExecutionLog]))
    )

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[Execution]] =
    for {
      executions <- core.findAll(
        ExecutionQueries.select,
        offset,
        limit,
        ExecutionQueries.limitAndOffset
      )
      executionIds   <- executions.map(_.id).toList.pure[F]
      logsExecutions <- Execution.loadExecutionLogs(executionIds)
    } yield enrichExecutionsWithLogs(executions, logsExecutions)

  def findById(id: UUID): F[Option[Execution]] =
    for {
      executionOpt <- core.findByID(ExecutionQueries.select, ExecutionQueries.where(id = id))
      logs <- executionOpt match {
        case Some(execution) => Execution.loadExecutionLogs(List(execution.id))
        case None            => MonadCancelThrow[F].pure(Seq.empty[ExecutionLog])
      }
    } yield executionOpt.map(execution => enrichExecutionsWithLogs(Seq(execution), logs).head)

  def create(pipeline: Execution): F[UUID] = core.create(ExecutionQueries.insert(pipeline))

  def update(id: UUID, pipeline: Execution): F[Option[Int]] =
    core.update(ExecutionQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(ExecutionQueries.delete(id))

object ExecutionsRepository:
  def apply[F[_]: Async: Logger: Parallel](using
      xa: Transactor[F],
      core: Core[F, Execution],
      executionLogsRepository: ExecutionLogsRepository[F]
  ): Resource[F, ExecutionsRepository[F]] =
    Resource.eval(Async[F].pure(new ExecutionsRepository[F]))
