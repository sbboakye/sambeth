package com.sbboakye.engine.repositories.execution

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.sbboakye.engine.domain.{ExecutionStatus, PipelineExecution, PipelineExecutionLog}
import com.sbboakye.engine.repositories.Repository
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import doobie.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class PipelineExecutionsRepository[F[_]: { MonadCancelThrow, Logger }] private (using
    xa: Transactor[F],
    helper: PipelineExecutionLogsHelper[F]
) extends Core[F, PipelineExecution]
    with Repository[F, PipelineExecution]:

  def findAll(
      offset: Int,
      limit: Int
  ): F[Seq[PipelineExecution]] =
    for {
      executions <- findAll(
        PipelineExecutionQueries.select,
        offset,
        limit,
        PipelineExecutionQueries.limitAndOffset
      )
      enrichExecutions <- helper.enrichExecutionsWithLogs(executions)
    } yield enrichExecutions

  def findById(id: UUID): F[Option[PipelineExecution]] =
    for {
      executionOpt <- findByID(
        PipelineExecutionQueries.select,
        PipelineExecutionQueries.where(id = id)
      )
      enrichExecution <- executionOpt match {
        case Some(execution) => helper.enrichExecutionsWithLogs(Seq(execution)).map(_.headOption)
        case None            => MonadCancelThrow[F].pure(None)
      }
    } yield enrichExecution

  def create(execution: PipelineExecution): F[UUID] =
    create(PipelineExecutionQueries.insert(execution))

  override def update(id: UUID, execution: PipelineExecution): F[Option[Int]] =
    update(PipelineExecutionQueries.update(id, execution))

  def delete(id: UUID): F[Option[Int]] = delete(PipelineExecutionQueries.delete(id))

object PipelineExecutionsRepository:
  def apply[F[_]: { MonadCancelThrow, Logger }](using
      Transactor[F],
      PipelineExecutionLogsHelper[F]
  ): Resource[F, PipelineExecutionsRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new PipelineExecutionsRepository[F]))
