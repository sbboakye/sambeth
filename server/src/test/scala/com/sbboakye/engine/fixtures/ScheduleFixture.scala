package com.sbboakye.engine.fixtures

import cats.syntax.all.*
import cats.kernel.Eq
import cats.data.NonEmptyChain
import com.sbboakye.engine.domain.{
  DomainValidation,
  InvalidCronExpression,
  InvalidTimezone,
  Schedule
}

trait ScheduleFixture:
  given Eq[DomainValidation] = Eq.fromUniversalEquals

  val validCronExpression: String = "0 0 12 * * ?"
  val validTimezone: String       = "UTC"
  val createValidSchedule: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(validCronExpression, validTimezone)

  val invalidCronExpression: String = "0 0 12 * *"
  val invalidTimezone: String       = "UTT"
  val createInvalidScheduleWithBothParametersInvalid
      : Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(invalidCronExpression, invalidTimezone)

  val createInvalidScheduleWithOnlyTimezoneValid
      : Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(invalidCronExpression, validTimezone)

  val createInvalidScheduleWithOnlyCronValid: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(validCronExpression, invalidTimezone)

  val allValidationsSet: Set[DomainValidation] = Set(InvalidCronExpression, InvalidTimezone)
