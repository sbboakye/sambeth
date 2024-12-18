package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.Schedule
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

class SchedulesRepositoryTests extends AsyncFreeSpec with AsyncIOSpec with Matchers with CoreSpec:

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
    }
  }
