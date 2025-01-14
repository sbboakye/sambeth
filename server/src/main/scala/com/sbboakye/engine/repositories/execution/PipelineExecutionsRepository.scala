package com.sbboakye.engine.repositories.execution

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.sbboakye.engine.domain.{PipelineExecution, PipelineExecutionLog, ExecutionStatus}
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import doobie.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class PipelineExecutionsRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, PipelineExecution]
):

  def findAll(
      offset: Int,
      limit: Int,
      helper: PipelineExecutionLogsHelper[F]
  ): F[Seq[PipelineExecution]] =
    for {
      executions <- core.findAll(
        PipelineExecutionQueries.select,
        offset,
        limit,
        PipelineExecutionQueries.limitAndOffset
      )
      enrichExecutions <- helper.enrichExecutionsWithLogs(executions)
    } yield enrichExecutions

  def findById(id: UUID, helper: PipelineExecutionLogsHelper[F]): F[Option[PipelineExecution]] =
    for {
      executionOpt <- core.findByID(
        PipelineExecutionQueries.select,
        PipelineExecutionQueries.where(id = id)
      )
      enrichExecution <- executionOpt match {
        case Some(execution) => helper.enrichExecutionsWithLogs(Seq(execution)).map(_.headOption)
        case None            => MonadCancelThrow[F].pure(None)
      }
    } yield enrichExecution

  def create(execution: PipelineExecution): F[UUID] =
    core.create(PipelineExecutionQueries.insert(execution))

  def update(id: UUID, execution: PipelineExecution): F[Option[Int]] =
    core.update(PipelineExecutionQueries.update(id, execution))

  def delete(id: UUID): F[Option[Int]] = core.delete(PipelineExecutionQueries.delete(id))

object PipelineExecutionsRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, PipelineExecution],
      executionLogsRepository: PipelineExecutionLogsRepository[F]
  ): Resource[F, PipelineExecutionsRepository[F]] =
    Resource.eval(Async[F].pure(new PipelineExecutionsRepository[F]))
