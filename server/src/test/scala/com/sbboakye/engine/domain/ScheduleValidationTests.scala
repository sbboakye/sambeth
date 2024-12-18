package com.sbboakye.engine.domain

import cats.data.ValidatedNec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleValidationTests extends AnyFreeSpec with Matchers {

  "Schedule validation logic" - {
    "validateCronExpression" - {
      "should pass for a valid cron expression" in {
        val validCron = "0 0 12 * * ?"
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateCronExpression(validCron)
        result.isValid shouldBe true
      }

      "should fail for an invalid cron expression" in {
        val invalidCron = "0 0 12 * *"
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateCronExpression(invalidCron)
        result.isInvalid shouldBe true
      }
    }

    "validateTimezone" - {
      "should pass for a valid timezone" in {
        val validTimezone = "America/New_York"
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateTimezone(validTimezone)
        result.isValid shouldBe true
      }

      "should fail for a invalid timezone" in {
        val invalidTimezone = "UTT"
        val result: ValidatedNec[DomainValidation, String] =
          Schedule.validateTimezone(invalidTimezone)
        result.isInvalid shouldBe true
      }
    }

    "validateSchedulerFields" - {
      "should pass for valid cron expression and timezone" in {
        val validCron     = "0 0 12 * * ?"
        val validTimezone = "UTC"
        val result        = Schedule.validateSchedulerFields(validCron, validTimezone)
        result.isValid shouldBe true
      }

      "should fail if either the cron expression is invalid but timezone is valid" in {
        val invalidCron   = "0 0 12 * *"
        val validTimezone = "UTC"
        val result        = Schedule.validateSchedulerFields(invalidCron, validTimezone)
        result.isInvalid shouldBe true
      }

      "should fail if either the cron expression is valid but timezone is invalid" in {
        val validCron       = "0 0 12 * * ?"
        val invalidTimezone = "UTT"
        val result2         = Schedule.validateSchedulerFields(validCron, invalidTimezone)
        result2.isInvalid shouldBe true
      }
    }
  }

}
