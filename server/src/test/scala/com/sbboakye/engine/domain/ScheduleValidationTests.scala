package com.sbboakye.engine.domain

import com.sbboakye.engine.fixtures.CoreFixture

import cats.data.ValidatedNec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleValidationTests extends AnyFreeSpec with Matchers with CoreFixture {

  "Schedule validation logic" - {
    "validateCronExpression" - {
      "should pass for a valid cron expression" in {
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateCronExpression(validCronExpression)
        result.isValid shouldBe true
      }

      "should fail for an invalid cron expression" in {
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateCronExpression(invalidCronExpression)
        result.isInvalid shouldBe true
      }
    }

    "validateTimezone" - {
      "should pass for a valid timezone" in {
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateTimezone(validTimezone)
        result.isValid shouldBe true
      }

      "should fail for a invalid timezone" in {
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateTimezone(invalidTimezone)
        result.isInvalid shouldBe true
      }
    }

    "validateSchedulerFields" - {
      "should pass for valid cron expression and timezone" in {
        val result = Schedule.validateSchedulerFields(validCronExpression, validTimezone)
        result.isValid shouldBe true
      }

      "should fail if either the cron expression is invalid but timezone is valid" in {
        val result = Schedule.validateSchedulerFields(invalidCronExpression, validTimezone)
        result.isInvalid shouldBe true
      }

      "should fail if either the cron expression is valid but timezone is invalid" in {
        val result2 = Schedule.validateSchedulerFields(validCronExpression, invalidTimezone)
        result2.isInvalid shouldBe true
      }
    }
  }

}
