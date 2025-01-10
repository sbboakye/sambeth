package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, PipelineId}
import com.sbboakye.engine.repositories.executionLog.ExecutionLogsRepository
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository
import doobie.Read
import doobie.implicits.*
import doobie.util.meta.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.time.OffsetDateTime

case class Execution(
    id: ExecutionId,
    pipelineId: PipelineId,
    startTime: OffsetDateTime,
    endTime: Option[OffsetDateTime],
    status: ExecutionStatus,
    logs: Seq[ExecutionLog],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def getPipeline[F[_]](using
      repository: PipelinesRepository[F]
  ): F[Option[Pipeline]] =
    repository.findById(pipelineId)
}

object Execution:
  given Read[Execution] = Read[
    (
        ExecutionId,
        PipelineId,
        OffsetDateTime,
        Option[OffsetDateTime],
        ExecutionStatus,
        OffsetDateTime,
        OffsetDateTime
    )
  ]
    .map {
      case (
            id,
            pipelineId,
            startTime,
            endTime,
            status,
            createdAt,
            updatedAt
          ) =>
        Execution(
          id,
          pipelineId,
          startTime,
          endTime,
          status,
          Seq.empty[ExecutionLog],
          createdAt,
          updatedAt
        )
    }

  def loadExecutionLogs[F[_]](listOfIds: List[ExecutionId])(using
      executionLogsRepository: ExecutionLogsRepository[F]
  ): F[Seq[ExecutionLog]] =
    executionLogsRepository.findAllByExecutionIds(listOfIds)
