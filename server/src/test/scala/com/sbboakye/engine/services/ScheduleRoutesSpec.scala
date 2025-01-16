package com.sbboakye.engine.services

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.contexts.RepositoryContext
import com.sbboakye.engine.domain.Schedule
import org.http4s.implicits.*
import org.http4s.{Method, Request, Response, Status, Uri}
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import com.sbboakye.engine.repositories.CoreSpec
import com.sbboakye.engine.routes.ScheduleRoutes
import io.circe.Json
import io.circe.syntax.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class ScheduleRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with CoreSpec:

  override val initSqlString: String = "sql/postgres.sql"
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
        check[Json](result, Status.Ok, Some(expectedJson)).asserting(_ shouldBe true)
      }
    }
  }
