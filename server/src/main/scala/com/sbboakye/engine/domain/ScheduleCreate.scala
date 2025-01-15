package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.ScheduleId

import java.time.OffsetDateTime

case class ScheduleCreate(
    cronExpression: String,
    timezone: String
)
