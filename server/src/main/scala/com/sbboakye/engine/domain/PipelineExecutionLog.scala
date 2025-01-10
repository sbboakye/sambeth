package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, ExecutionLogId, StageId}
import com.sbboakye.engine.repositories.execution.ExecutionsRepository
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.postgres.implicits.*

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
      repository: ExecutionsRepository[F]
  ): F[Option[Execution]] =
    repository.findById(executionId)

  def getStage[F[_]](using
      repository: StagesRepository[F]
  ): F[Option[Stage]] =
    repository.findById(stageId)
}
