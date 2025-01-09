//package com.sbboakye.engine.repositories.execution
//
//import cats.*
//import cats.instances.seq.*
//import cats.effect.*
//import cats.syntax.all.*
//import cats.syntax.parallel.*
//import com.sbboakye.engine.domain.CustomTypes.ExecutionPipelineExecutionLogJoined
//import com.sbboakye.engine.domain.{Connector, ConnectorType, Execution, ExecutionLog, ExecutionStatus, Pipeline, Schedule, Stage, StageType}
//import com.sbboakye.engine.repositories.core.Core
//import doobie.*
//import doobie.implicits.*
//import doobie.generic.auto.*
//import doobie.postgres.*
//import doobie.postgres.implicits.*
//import org.typelevel.log4cats.Logger
//
//import java.time.OffsetDateTime
//import java.util.UUID
//
//class ExecutionsRepository[F[_]: MonadCancelThrow: Logger: Parallel] private (using
//    xa: Transactor[F],
//    core: Core[F, Execution]
//):
//  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given
//
//  private def enrichExecutions[A](
//      fetchExecutions: => F[Seq[ExecutionPipelineExecutionLogJoined]]
//  ): F[Seq[Execution]] =
//    fetchExecutions.flatMap(rows => {
//      // Group rows by execution ID
//      val grouped = rows.groupBy(_.executionId)
//
//      // Traverse each execution and its associated rows
//      grouped.toSeq.parTraverse { case (executionId, executionRows) =>
//        // Extract execution and schedule details from the first row
//        val firstRow = executionRows.head
//
//        // Extract pipelines and their execution logs
//        val logs = executionRows.flatMap { row =>
//          val executionLog = ExecutionLog(
//            id = row.executionLogId,
//            stageId = row.logStageId,
//            timestamp = row.logTimestamp,
//            message = row.logMessage,
//            logLevel = row.logLevel,
//            createdAt = row.logCreatedAt
//          )
//
//          val pipeline = Pipeline(
//            id = row.pipelineId,
//            name = row.pipelineName,
//            description = row.pipelineDescription,
//            stages = ???,
//            schedule = row.pipelineScheduleId,
//            status = row.pipelineStatus,
//            createdAt = ,
//            updatedAt = ,
//          )
//        }
//
//        // Create Schedule object if schedule fields are present
//        val schedule = for {
//          id             <- firstRow.scheduleId
//          cronExpression <- firstRow.scheduleCronExpression
//          timezone       <- firstRow.scheduleTimezone
//          createdAt      <- firstRow.scheduleCreatedAt
//          updatedAt      <- firstRow.scheduleUpdatedAt
//        } yield Schedule(
//          id = id,
//          cronExpression = cronExpression,
//          timezone = timezone,
//          createdAt = createdAt,
//          updatedAt = updatedAt
//        )
//
//        // Combine and return the enriched Execution
//        MonadCancelThrow[F].pure(
//          Execution(
//            id = firstRow.pipelineId,
//            name = firstRow.pipelineName,
//            description = firstRow.pipelineDescription,
//            stages = stages,
//            schedule = schedule,
//            status = firstRow.pipelineStatus,
//            createdAt = firstRow.pipelineCreatedAt,
//            updatedAt = firstRow.pipelineUpdatedAt
//          )
//        )
//      }
//    })
//
//  def findAll(
//      offset: Int,
//      limit: Int
//  ): F[Seq[Execution]] =
//    fetchExecutionsWithConnectorsJoin(offset: Int, limit: Int)
//
//  private def fetchExecutionsWithConnectorsJoin(offset: Int, limit: Int): F[Seq[Execution]] =
//
//    def getExecutions: F[
//      Seq[ExecutionPipelineExecutionLogJoined]
//    ] =
//      (ExecutionQueries.select ++ ExecutionQueries.limitAndOffset(offset, limit))
//        .query[ExecutionPipelineExecutionLogJoined]
//        .to[Seq]
//        .transact(xa)
//
//    enrichExecutions(getExecutions)
//
//  def findById(id: UUID): F[Option[Execution]] =
//    fetchExecutionWithConnectors(id)
//
//  private def fetchExecutionWithConnectors(id: UUID): F[Option[Execution]] =
//
//    def getExecution: F[
//      Option[ExecutionPipelineExecutionLogJoined]
//    ] =
//      (ExecutionQueries.select ++ ExecutionQueries.where(column = "e.id", id = id))
//        .query[ExecutionPipelineExecutionLogJoined]
//        .option
//        .transact(xa)
//
//    getExecution.flatMap {
//      case Some(pipeline) =>
//        enrichExecutions(MonadCancelThrow[F].pure(Seq(pipeline))).map(_.headOption)
//      case None => MonadCancelThrow[F].pure(None)
//    }
//
//  def create(pipeline: Execution): F[UUID] = core.create(ExecutionQueries.insert(pipeline))
//
//  def update(id: UUID, pipeline: Execution): F[Option[Int]] =
//    core.update(ExecutionQueries.update(id, pipeline))
//
//  def delete(id: UUID): F[Option[Int]] = core.delete(ExecutionQueries.delete(id))
//
//object ExecutionsRepository:
//  def apply[F[_]: Async: Logger: Parallel](using
//      xa: Transactor[F],
//      core: Core[F, Execution]
//  ): Resource[F, ExecutionsRepository[F]] =
//    Resource.eval(Async[F].pure(new ExecutionsRepository[F]))
