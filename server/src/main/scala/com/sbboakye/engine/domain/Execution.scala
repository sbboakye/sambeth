package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.ExecutionId

import java.time.OffsetDateTime

case class Execution(
    id: ExecutionId,
    pipeline: Pipeline,
    startTime: OffsetDateTime,
    endTime: Option[OffsetDateTime],
    status: ExecutionStatus,
    logs: List[ExecutionLog],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
