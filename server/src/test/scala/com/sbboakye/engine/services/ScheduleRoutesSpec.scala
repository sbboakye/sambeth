package com.sbboakye.engine.services

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.fixtures.CoreFixture
import org.http4s.{Method, Status}
import com.sbboakye.engine.repositories.CoreSpec
import com.sbboakye.engine.routes.ScheduleRoutes
import doobie.Transactor
import io.circe.Json
import io.circe.syntax.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  override val initSqlString: String       = "sql/postgres.sql"
  private val additionSQLScript: String    = "schedules.sql"
  private val relevantFields: List[String] = List("id", "cronExpression", "timezone")
  private val entity                       = "schedules"
  import repositoryContext.schedulesRepositorySetup

  "ScheduleRoutes API" - {
    "listAPIRoute" - {
      "should return an empty list when no schedules exist" in {
        val expectedJson = Json.obj(
          "data" := Json.arr()
        )
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.Ok,
          endpoint = s"/$entity",
          expectedJson = Some(expectedJson),
          fieldsToCompare = relevantFields
        ) { xa =>
          ScheduleService[IO](xa).use { service =>
            IO.pure(ScheduleRoutes[IO](service).routes)
          }
        }.asserting(_ shouldBe true)
      }

      "should return a list of schedules when they exist" in {
        val expectedJson = Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "id"             -> Json.fromString("66666666-6666-6666-6666-666666666661"),
              "cronExpression" -> Json.fromString("0 12 * * *"),
              "timezone"       -> Json.fromString("UTC")
            ),
            Json.obj(
              "id"             -> Json.fromString("66666666-6666-6666-6666-666666666662"),
              "cronExpression" -> Json.fromString("0 6 * * 1"),
              "timezone"       -> Json.fromString("America/New_York")
            )
          )
        )
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.Ok,
          endpoint = s"/$entity",
          expectedJson = Some(expectedJson),
          fieldsToCompare = relevantFields
        ) { xa =>
          ScheduleService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              - <- executeSqlScript(additionSQLScript)
            } yield ScheduleRoutes[IO](service).routes
          }
        }.asserting(_ shouldBe true)
      }
    }

    "detailAPIRoute" - {
      "should return nothing and a status of not found if the schedule does not exist" in {
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.NotFound,
          endpoint = s"/$entity/66666666-0000-6666-6666-666666666681",
          fieldsToCompare = relevantFields,
          onlyStatus = true
        ) { xa =>
          ScheduleService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _ <- executeSqlScript(additionSQLScript)
            } yield ScheduleRoutes[IO](service).routes
          }
        }.asserting(_ shouldBe true)
      }
      "should return a schedule and a status of 200 if the schedule exists" in {
        val expectedJson = Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "id"             -> Json.fromString("66666666-6666-6666-6666-666666666661"),
              "cronExpression" -> Json.fromString("0 12 * * *"),
              "timezone"       -> Json.fromString("UTC")
            )
          )
        )
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.Ok,
          endpoint = "/schedules/66666666-6666-6666-6666-666666666661",
          expectedJson = Some(expectedJson),
          fieldsToCompare = relevantFields
        ) { xa =>
          ScheduleService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _ <- executeSqlScript(additionSQLScript)
            } yield ScheduleRoutes[IO](service).routes
          }
        }.asserting(_ shouldBe true)
      }
    }

    "createAPIRoute" - {
      "should create a new schedule and return its id" in {
        val validScheduleJson = Json.obj(
          "cronExpression" -> Json.fromString(validSchedule.cronExpression),
          "timezone"       -> Json.fromString(validSchedule.timezone)
        )
        testAPIEndpoints(
          httpMethod = Method.POST,
          httpStatus = Status.Created,
          endpoint = s"/$entity/create",
          maybeBody = Some(validScheduleJson),
          onlyStatus = true
        ) { xa =>
          ScheduleService[IO](xa).use { service =>
            given Transactor[IO] = xa

            for {
              _ <- executeSqlScript(additionSQLScript)
            } yield ScheduleRoutes[IO](service).routes
          }
        }.asserting(_ shouldBe true)
      }
    }
  }
