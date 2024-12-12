package com.sbboakye.engine.domain

import cats.syntax.all.*
import cats.data.{NonEmptyChain, ValidatedNec}
import cats.data.Validated.{Invalid, Valid}
import com.sbboakye.engine.domain.CustomTypes.ScheduleId
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser

import java.time.{OffsetDateTime, ZoneId}
import java.util.UUID

case class Schedule(
    id: ScheduleId = UUID.randomUUID(),
    cronExpression: String,
    timezone: String,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: OffsetDateTime = OffsetDateTime.now()
)

object Schedule:
  private type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private def validateCronExpression(cronExpression: String): ValidationResult[String] =
    val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
    val cronParser     = new CronParser(cronDefinition)

    try
      val parsedCron = cronParser.parse(cronExpression)
      parsedCron.validate()
      cronExpression.validNec
    catch case e: Exception => InvalidCronExpression.invalidNec

  private def validateTimezone(timezone: String): ValidationResult[String] =
    try
      ZoneId.of(timezone)
      timezone.validNec
    catch case e: Exception => InvalidTimezone.invalidNec

  private def validateSchedulerFields(
      cronExpression: String,
      timezone: String
  ): ValidationResult[(String, String)] = {
    (validateCronExpression(cronExpression), validateTimezone(timezone)).mapN(
      (validCron, validTimezone) => (validCron, validTimezone)
    )
  }

  def create(
      cronExpression: String,
      timezone: String
  ): Either[NonEmptyChain[DomainValidation], Schedule] =
    val validationResult = validateSchedulerFields(cronExpression, timezone).toEither
    validationResult match
      case Right((cronExpression, timezone)) =>
        Schedule(cronExpression = cronExpression, timezone = timezone).asRight
      case Left(e) => e.asLeft
