package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.ScheduleId

import java.time.OffsetDateTime

case class Schedule(
    id: ScheduleId,
    cronExpression: String,
    timezone: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
