package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, PipelineId}

import java.time.OffsetDateTime

case class Execution(
    id: ExecutionId,
    pipelineId: PipelineId,
    startTime: OffsetDateTime,
    endTime: Option[OffsetDateTime],
    status: ExecutionStatus,
    logs: List[ExecutionLog]
)
