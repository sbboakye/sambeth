package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import com.sbboakye.engine.contexts.RepositoryContext.{schedulesRepositorySetup, NoHelper}
import org.scalatest.Assertion

import java.util.UUID

class SchedulesRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  override val initSqlString: String = "sql/postgres.sql"

  "SchedulesRepository" - {
    "findAll" - {
      "should return an empty list when no schedules exist" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          repo.findAll(0, 10).asserting(_ shouldBe empty)
        }
      }

      "should return a list of schedules when schedules exist" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = for {
            _           <- repo.create(validSchedule)
            queryResult <- repo.findAll(0, 10)
          } yield queryResult
          result.asserting(_ should not be empty)
        }
      }
    }

    "findById" - {
      "should return None if the schedule does not exist" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.findById(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }

      "should return the correct schedule if the schedule exists" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = for {
            uuid        <- repo.create(validSchedule)
            queryResult <- repo.findById(uuid)
          } yield (queryResult, uuid)
          result.asserting((queryResult, uuid) => {
            queryResult.get.id shouldBe uuid
          })
        }
      }
    }

    "create" - {
      "should create a new schedule and return its id" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.create(validSchedule)
          result.asserting(_ should not be null)
        }
      }
    }

    "update" - {
      "should update an existing schedule" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = for {
            id <- repo.create(validSchedule)
            updatedScheduleObject <- validSchedule
              .copy(cronExpression = validUpdateCronExpression)
              .pure[IO]
            updatedSchedule <- repo.update(id, updatedScheduleObject)
          } yield updatedSchedule
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if schedule does not exist" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.update(nonExistentId, validSchedule)
          result.asserting(_ shouldBe None)
        }
      }
    }

    "delete" - {
      "should delete an existing schedule" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = for {
            id           <- repo.create(validSchedule)
            deleteResult <- repo.delete(id)
          } yield deleteResult
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if schedule does not exist" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.delete(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }
    }

    "Transactional behaviour" - {
      "should rollback on failure" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val transactionalResult = (for {
            _         <- repo.create(validSchedule)
            _         <- repo.create(invalidScheduleWithNullCronExpression)
            schedules <- repo.findAll(0, 10)
          } yield schedules).attempt
          transactionalResult.asserting(_.isLeft shouldBe true)
        }
      }
    }

    "Edge Cases: Empty or Null Data" - {
      "should fail when inserting a schedule with null cron expression" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.create(invalidScheduleWithNullCronExpression).attempt
          result.asserting(_.left.toSeq.exists {
            case _: Throwable => true
            case null         => false
          } shouldBe true)
        }
      }

      "should fail when inserting a schedule with null timezone" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.create(invalidScheduleWithNullTimezone).attempt
          result.asserting(_.left.toSeq.exists {
            case _: Throwable => true
            case null         => false
          } shouldBe true)
        }
      }

      "should fail when inserting a schedule with empty cron expression" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.create(invalidScheduleWithEmptyCronExpression).attempt
          result.asserting(_.left.toSeq.exists {
            case _: Throwable => true
            case null         => false
          } shouldBe true)
        }
      }

      "should fail when inserting a schedule with empty timezone" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.create(invalidScheduleWithEmptyTimezone).attempt
          result.asserting(_.left.toSeq.exists {
            case _: Throwable => true
            case null         => false
          } shouldBe true)
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val results = for {
            randomSchedules <- List.fill(10)(validSchedule.copy(id = UUID.randomUUID())).pure[IO]
            inserts   <- randomSchedules.parTraverse(randomSchedule => repo.create(randomSchedule))
            schedules <- repo.findAll(0, 20)
          } yield (inserts, schedules)
          results.asserting(_._1.size shouldBe 10)
          results.asserting(_._2.size shouldBe 10)
        }
      }

      "should handle concurrent updates correctly" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val results = for {
            scheduleId <- repo.create(validSchedule)
            schedules <- List
              .fill(10)(
                validSchedule.copy(id = scheduleId, cronExpression = validUpdateCronExpression)
              )
              .pure[IO]
            updates         <- schedules.parTraverse(schedule => repo.update(scheduleId, schedule))
            fetchedSchedule <- repo.findById(scheduleId)
          } yield (updates, fetchedSchedule)
          results.asserting(_._1.flatMap(_.toList).reduceLeftOption(_ + _) shouldBe Option(10))
          results.asserting(_._2.get.cronExpression shouldBe validUpdateCronExpression)
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        withDependencies[SchedulesRepository, NoHelper, Assertion] { (repo, _, _) =>
          val results = for {
            randomSchedules <- List
              .fill(1000)(validSchedule.copy(id = UUID.randomUUID()))
              .pure[IO]
            inserts   <- randomSchedules.parTraverse(schedule => repo.create(schedule))
            schedules <- repo.findAll(0, 1000)
          } yield schedules
          results.asserting(_.size shouldBe 1000)
        }
      }
    }
  }
