package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.ScheduleId

import java.time.OffsetDateTime
import java.util.UUID

case class Schedule(
    id: UUID,
    cronExpression: String,
    timezone: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
