package com.sbboakye.engine.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleCreationTests extends AnyFreeSpec with Matchers {

  "Schedule creation logic" - {
    "create" - {
      "should return a Schedule instance for valid inputs" in {
        val validCron     = "0 0 12 * * ?"
        val validTimezone = "UTC"
        val result        = Schedule.create(validCron, validTimezone)

        result.isRight shouldBe true
      }

      "should return an error for invalid cron expression" in {
        val invalidCron   = "0 0 12 * *"
        val validTimezone = "UTC"
        val result        = Schedule.create(invalidCron, validTimezone)

        result.isLeft shouldBe true
      }

      "should return an error for invalid timezone" in {
        val validCron       = "0 0 12 * * ?"
        val invalidTimezone = "UTT"
        val result          = Schedule.create(validCron, invalidTimezone)

        result.isLeft shouldBe true
      }

      "should return an error for both invalid cron expression and timezone" in {
        val invalidCron     = "0 0 12 * *"
        val invalidTimezone = "UTT"
        val result          = Schedule.create(invalidCron, invalidTimezone)

        result.isLeft shouldBe true
      }
    }
  }

}
