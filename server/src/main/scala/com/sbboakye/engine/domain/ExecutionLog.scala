package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionLogId, StageId}

import java.time.OffsetDateTime

case class ExecutionLog(
    id: ExecutionLogId,
    stage: Stage,
    timestamp: OffsetDateTime,
    message: String,
    logLevel: LogLevel,
    createdAt: OffsetDateTime
)
