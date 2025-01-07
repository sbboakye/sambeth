package com.sbboakye.engine.domain

import com.sbboakye.engine.fixtures.CoreFixture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleCreationTests extends AnyFreeSpec with Matchers with CoreFixture {

  "Schedule creation logic" - {
    "create" - {
      "should return a Right(Schedule) for valid inputs" in {
        val result = createValidSchedule
        result.isRight shouldBe true
      }

      "should return a Schedule instance with the same valid cron expression used in its creation" in {
        val result = createValidSchedule
        result.foreach { schedule =>
          schedule.cronExpression shouldBe validCronExpression
        }
      }

      "should return a Schedule instance with the same valid timezone used in its creation" in {
        val result = createValidSchedule
        result.map { schedule =>
          schedule.timezone shouldBe validTimezone
        }
      }

      "should return a Left for invalid cron expression" in {
        val result = createInvalidScheduleWithOnlyTimezoneValid
        result.isLeft shouldBe true
      }

      "should return a non-empty chain containing InvalidCronExpression DomainValidation for invalid cron expression" in {
        val result = createInvalidScheduleWithOnlyTimezoneValid
        result.left.foreach { errors =>
          errors.contains(InvalidCronExpression) shouldBe true
        }
      }

      "should return a Left for invalid timezone" in {
        val result = createInvalidScheduleWithOnlyCronValid
        result.isLeft shouldBe true
      }

      "should return a non-empty chain containing InvalidTimezone DomainValidation for invalid timezone" in {
        val result = createInvalidScheduleWithOnlyCronValid
        result.left.foreach { errors =>
          errors.contains(InvalidTimezone) shouldBe true
        }
      }

      "should return a Left for both invalid cron expression and timezone" in {
        val result = createInvalidScheduleWithBothParametersInvalid
        result.isLeft shouldBe true
      }

      "should return a non-empty chain containing both InvalidCronExpression and InvalidTimezone DomainValidations when both parameters are invalid" in {
        val result = createInvalidScheduleWithBothParametersInvalid
        result.left.foreach { errors =>
          errors.toNonEmptyList.toList.toSet.subsetOf(allValidationsSet) shouldBe true
        }
      }
    }
  }

}
