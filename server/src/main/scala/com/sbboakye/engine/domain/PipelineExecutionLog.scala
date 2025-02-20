package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, ExecutionLogId, StageId}
import com.sbboakye.engine.repositories.execution.{
  PipelineExecutionLogsHelper,
  PipelineExecutionsRepository
}
import com.sbboakye.engine.repositories.stage.{ConnectorsHelper, StagesRepository}

import java.time.OffsetDateTime

case class PipelineExecutionLog(
    id: ExecutionLogId,
    executionId: ExecutionId,
    stageId: StageId,
    timestamp: OffsetDateTime,
    message: String,
    logLevel: LogLevel,
    createdAt: OffsetDateTime
) {
  def getExecution[F[_]](using
      repository: PipelineExecutionsRepository[F]
  ): F[Option[PipelineExecution]] =
    repository.findById(executionId)

  def getStage[F[_]](using
      repository: StagesRepository[F]
  ): F[Option[Stage]] =
    repository.findById(stageId)
}
