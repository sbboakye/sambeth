package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.StageId

import java.time.OffsetDateTime

case class ExecutionLog(
    stageId: StageId,
    timestamp: OffsetDateTime,
    message: String,
    logLevel: LogLevel
)
