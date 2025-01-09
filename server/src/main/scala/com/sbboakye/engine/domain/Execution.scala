package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, PipelineId}
import com.sbboakye.engine.repositories.executionLog.ExecutionLogsRepository
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository

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
  def loadExecutionLogs[F[_]](listOfIds: List[ExecutionId])(using
      executionLogsRepository: ExecutionLogsRepository[F]
  ): F[Seq[ExecutionLog]] =
    executionLogsRepository.findAllByExecutionIds(listOfIds)
