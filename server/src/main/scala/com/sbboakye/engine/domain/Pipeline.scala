package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.PipelineId

import java.time.OffsetDateTime

case class Pipeline(
    id: PipelineId,
    name: String,
    description: Option[String],
    stages: Seq[Stage],
    schedule: Option[Schedule],
    status: PipelineStatus,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
