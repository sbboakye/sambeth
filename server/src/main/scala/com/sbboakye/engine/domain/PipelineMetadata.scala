package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.PipelineId

import java.time.OffsetDateTime

case class PipelineMetadata(
    id: PipelineId,
    name: String,
    description: Option[String],
    schedule: Option[String],
    status: PipelineStatus,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
