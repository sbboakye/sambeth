package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.PipelineId

import java.time.OffsetDateTime

case class Pipeline(
    id: PipelineId,
    name: String,
    description: Option[String],
    stages: List[Stage],
    schedule: Option[Schedule],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
    status: PipelineStatus
)
