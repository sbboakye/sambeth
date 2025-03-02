package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.ScheduleId
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.OffsetDateTime

case class ScheduleCreate(
    cronExpression: String,
    timezone: String
)

object ScheduleCreate:
  given Encoder[ScheduleCreate] = deriveEncoder[ScheduleCreate]
  given Decoder[ScheduleCreate] = deriveDecoder[ScheduleCreate]
