package com.sbboakye.engine.services

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.CoreSpec
import com.sbboakye.engine.routes.PipelineRoutes
import doobie.Transactor
import io.circe.syntax.*
import io.circe.Json
import org.http4s.{Method, Status}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class PipelineRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:
  override val initSqlString: String       = "sql/postgres.sql"
  private val additionSQLScript: String    = "pipelines.sql"
  private val relevantFields: List[String] = List("id", "name", "description", "scheduleId")
  private val entity: String               = "pipelines"
  import repositoryContext.pipelinesRepositorySetup

  "PipelineRoutes API" - {
    "listAPIRoute" - {
      "should return an empty list when no pipelines exist" in {
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
          PipelineService[IO](xa).use { service =>
            PipelineRoutes[IO](service).use { router =>
              IO.pure(router.routes)
            }
          }
        }.asserting(_ shouldBe true)
      }

      "should return a list of pipelines when pipelines exist" in {
        val expectedJson = Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "id"          -> Json.fromString("11111111-1111-1111-1111-111111111111"),
              "name"        -> Json.fromString("Pipeline 1"),
              "description" -> Json.fromString("Description for Pipeline 1"),
              "scheduleId"  -> Json.Null
            ),
            Json.obj(
              "id"          -> Json.fromString("11111111-1111-1111-1111-111111111112"),
              "name"        -> Json.fromString("Pipeline 2"),
              "description" -> Json.fromString("Description for Pipeline 2"),
              "scheduleId"  -> Json.Null
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
          PipelineService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _      <- executeSqlScript(additionSQLScript)
              router <- PipelineRoutes[IO](service).use(IO.pure)
            } yield router.routes
          }
        }.asserting(_ shouldBe true)
      }
    }
    "detailAPIRoute" - {
      "should return nothing and a status of not found if the pipeline does not exist" in {
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.NotFound,
          endpoint = s"/$entity/11111111-0000-1111-1111-111111111111",
          fieldsToCompare = relevantFields,
          onlyStatus = true
        ) { xa =>
          PipelineService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _      <- executeSqlScript(additionSQLScript)
              router <- PipelineRoutes[IO](service).use(IO.pure)
            } yield router.routes
          }
        }.asserting(_ shouldBe true)
      }
      "should return a pipeline and a status of 200 if the pipeline exists" in {
        val expectedJson = Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "id"          -> Json.fromString("11111111-1111-1111-1111-111111111111"),
              "name"        -> Json.fromString("Pipeline 1"),
              "description" -> Json.fromString("Description for Pipeline 1"),
              "scheduleId"  -> Json.Null
            )
          )
        )
        testAPIEndpoints(
          httpMethod = Method.GET,
          httpStatus = Status.Ok,
          endpoint = "/pipelines/11111111-1111-1111-1111-111111111111",
          expectedJson = Some(expectedJson),
          fieldsToCompare = relevantFields
        ) { xa =>
          PipelineService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _      <- executeSqlScript(additionSQLScript)
              router <- PipelineRoutes[IO](service).use(IO.pure)
            } yield router.routes
          }
        }.asserting(_ shouldBe true)
      }
    }
    "createAPIRoute" - {
      "should create a new pipeline and return its id" in {
        val validPipelineJson = Json.obj(
          "id"          -> Json.fromString("11111111-1111-1111-1111-111111111113"),
          "name"        -> Json.fromString("Pipeline 3"),
          "description" -> Json.fromString("Description for Pipeline 3"),
          "scheduleId"  -> Json.Null,
          "status"      -> Json.fromString("Draft")
        )
        testAPIEndpoints(
          httpMethod = Method.POST,
          httpStatus = Status.Created,
          endpoint = s"/$entity/create",
          maybeBody = Some(validPipelineJson),
          onlyStatus = true
        ) { xa =>
          PipelineService[IO](xa).use { service =>
            given Transactor[IO] = xa
            for {
              _      <- executeSqlScript(additionSQLScript)
              router <- PipelineRoutes[IO](service).use(IO.pure)
            } yield router.routes
          }
        }.asserting(_ shouldBe true)
      }
    }
    "updateAPIRoute" - {}
    "deleteAPIRoute" - {}
  }
