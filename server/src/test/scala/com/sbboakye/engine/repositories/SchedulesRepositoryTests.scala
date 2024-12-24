package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.fixtures.ScheduleFixture
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class SchedulesRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with ScheduleFixture:

  override val initSqlString: String = "sql/schedules.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  given Core[IO, Schedule] with {}

  "SchedulesRepository" - {
    "findAll" - {
      "should return an empty list when no schedules exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = repo.findAll(0, 10)
            result.asserting(_ shouldBe empty)
          }
        }
      }

      "should return a list of schedules when schedules exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = for {
              _           <- repo.create(validSchedule)
              queryResult <- repo.findAll(0, 10)
            } yield queryResult
            result.asserting(_ should not be empty)
          }
        }
      }
    }

    "findById" - {
      "should return None if the schedule does not exist" - {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = repo.findById(nonExistentScheduleId)
            result.asserting(_ shouldBe None)
          }
        }
      }

      "should return the correct schedule if the schedule exists" - {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
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
    }

    "create" - {
      "should create a new schedule and return its id" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = repo.create(validSchedule)
            result.asserting(_ should not be null)
          }
        }
      }
    }

    "update" - {
      "should update an existing schedule" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = for {
              id <- repo.create(validSchedule)
              updatedScheduleObject <- IO.pure(
                validSchedule.copy(cronExpression = validUpdateCronExpression)
              )
              updatedSchedule <- repo.update(id, updatedScheduleObject)
            } yield updatedSchedule
            result.asserting(_.get should be > 0)
          }
        }
      }

      "should return None is schedule does not exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = repo.update(nonExistentScheduleId, validSchedule)
            result.asserting(_ shouldBe None)
          }
        }
      }
    }

    "delete" - {
      "should delete an existing schedule" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = for {
              id           <- repo.create(validSchedule)
              deleteResult <- repo.delete(id)
            } yield deleteResult
            result.asserting(_.get should be > 0)
          }
        }
      }

      "should return None if schedule does not exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val result = repo.delete(nonExistentScheduleId)
            result.asserting(_ shouldBe None)
          }
        }
      }
    }

    "Transactional behaviour" - {
      "should rollback on failure" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          SchedulesRepository[IO].use { repo =>
            val transactionalResult = (for {
              _         <- repo.create(validSchedule)
              _         <- repo.create(invalidSchedule)
              schedules <- repo.findAll(0, 10)
            } yield schedules).attempt
            transactionalResult.asserting(_.isLeft shouldBe true)
          }
        }
      }
    }
  }
