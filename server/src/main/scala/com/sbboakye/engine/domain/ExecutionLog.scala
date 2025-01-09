package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, ExecutionLogId, StageId}

import java.time.OffsetDateTime

case class ExecutionLog(
    id: ExecutionLogId,
    stageId: StageId,
    timestamp: OffsetDateTime,
    message: String,
    logLevel: LogLevel,
    createdAt: OffsetDateTime
)
