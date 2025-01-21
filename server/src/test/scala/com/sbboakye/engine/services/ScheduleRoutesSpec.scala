package com.sbboakye.engine.services

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.circe.*
import com.sbboakye.engine.repositories.CoreSpec
import com.sbboakye.engine.routes.ScheduleRoutes
import doobie.Transactor
import io.circe.Json
import io.circe.syntax.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with CoreSpec:

  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript: String      = "schedules.sql"
  val relevantFields: List[String]   = List("id", "cronExpression", "timezone")
  import repositoryContext.schedulesRepositorySetup

  "SchedulesRoutes API" - {
    "listAPIRoute" - {
      "should return an empty list when no schedules exist" in {
        val result = coreSpecTransactor.use { xa =>
          ScheduleService[IO](xa).use { service =>
            val routes   = ScheduleRoutes[IO](service).routes
            val request  = Request[IO](Method.GET, Uri.unsafeFromString("/schedules"))
            val response = routes.run(request).value.map(_.get)
            response
          }
        }
        val expectedJson = Json.obj(
          "data" := Json.arr()
        )
        check(result, Status.Ok, Some(expectedJson), relevantFields).asserting(_ shouldBe true)
      }

      "should return a list of schedules when they exist" in {
        val result = coreSpecTransactor.use { xa =>
          given Transactor[IO] = xa
          ScheduleService[IO](xa).use { service =>
            val routes  = ScheduleRoutes[IO](service).routes
            val request = Request[IO](Method.GET, Uri.unsafeFromString("/schedules"))
            for {
              -        <- executeSqlScript(additionSQLScript)
              response <- routes.run(request).value.map(_.get)
            } yield response
          }
        }

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
        check(result, Status.Ok, Some(expectedJson), relevantFields).asserting(_ shouldBe true)
      }
    }
  }
