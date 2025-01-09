package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.{Connector, Pipeline, Stage}
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class PipelinesRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript: String      = "schedules.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  given Core[IO, Pipeline] with  {}
  given Core[IO, Stage] with     {}
  given Core[IO, Connector] with {}

  def withDependencies[T](test: (PipelinesRepository[IO], Transactor[IO]) => IO[T]): IO[T] =
    coreSpecTransactor.use { xa =>
      given Transactor[IO] = xa
      ConnectorsRepository[IO].use { cRepo =>
        given ConnectorsRepository[IO] = cRepo
        StagesRepository[IO].use { sRepo =>
          given StagesRepository[IO] = sRepo
          PipelinesRepository[IO].use { repo =>
            test(repo, xa)
          }
        }
      }
    }

  "PipelinesRepository" - {
    "findAll" - {
      "should return an empty list when no pipelines exist" in {
        withDependencies { (repo, xa) =>
          repo.findAll(0, 10).asserting(_ shouldBe empty)
        }
      }

      "should return a list of pipelines when pipelines exist" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -           <- executeSqlScript(additionSQLScript)(using xa)
            _           <- repo.create(pipeline1)
            _           <- repo.create(pipeline2)
            queryResult <- repo.findAll(0, 10)
          } yield queryResult
          result.asserting(_ should not be empty)
        }
      }
    }

    "findById" - {
      "should return None if the pipeline does not exist" in {
        withDependencies { (repo, xa) =>
          val result = repo.findById(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }

      "should return the correct pipeline if the pipeline exists" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -           <- executeSqlScript(additionSQLScript)(using xa)
            uuid        <- repo.create(pipeline1)
            queryResult <- repo.findById(uuid)
          } yield (queryResult, uuid)
          result.asserting((queryResult, uuid) => {
            queryResult.get.id shouldBe uuid
          })
        }
      }
    }

    "create" - {
      "should create a new pipeline and return its id" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -           <- executeSqlScript(additionSQLScript)(using xa)
            queryResult <- repo.create(pipeline1)
          } yield queryResult
          result.asserting(_ should not be null)
        }
      }
    }

    "update" - {
      "should update an existing pipeline" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -  <- executeSqlScript(additionSQLScript)(using xa)
            id <- repo.create(pipeline1)
            updatedPipelineObject <- pipeline1
              .copy(description = updatePipelineDescription)
              .pure[IO]
            updatedPipeline <- repo.update(id, updatedPipelineObject)
          } yield updatedPipeline
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if pipeline does not exist" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -           <- executeSqlScript(additionSQLScript)(using xa)
            queryResult <- repo.update(nonExistentId, pipeline1)
          } yield queryResult
          result.asserting(_ shouldBe None)
        }
      }
    }

    "delete" - {
      "should delete an existing pipeline" in {
        withDependencies { (repo, xa) =>
          val result = for {
            -            <- executeSqlScript(additionSQLScript)(using xa)
            id           <- repo.create(pipeline1)
            deleteResult <- repo.delete(id)
          } yield deleteResult
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if pipeline does not exist" in {
        withDependencies { (repo, xa) =>
          val result = repo.delete(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        withDependencies { (repo, xa) =>
          val results = for {
            -               <- executeSqlScript(additionSQLScript)(using xa)
            randomPipelines <- List.fill(10)(pipeline1.copy(id = UUID.randomUUID())).pure[IO]
            inserts   <- randomPipelines.parTraverse(randomPipeline => repo.create(randomPipeline))
            pipelines <- repo.findAll(0, 20)
          } yield (inserts, pipelines)
          results.asserting(_._1.size shouldBe 10)
          results.asserting(_._2.size shouldBe 10)
        }
      }

      "should handle concurrent updates correctly" in {
        withDependencies { (repo, xa) =>
          val results = for {
            -          <- executeSqlScript(additionSQLScript)(using xa)
            pipelineId <- repo.create(pipeline1)
            pipelines <- List
              .fill(10)(
                pipeline1.copy(id = pipelineId, description = updatePipelineDescription)
              )
              .pure[IO]
            updates         <- pipelines.parTraverse(pipeline => repo.update(pipelineId, pipeline))
            fetchedPipeline <- repo.findById(pipelineId)
          } yield (updates, fetchedPipeline)
          results.asserting(_._1.flatMap(_.toList).reduceLeftOption(_ + _) shouldBe Option(10))
          results.asserting(_._2.get.description shouldBe updatePipelineDescription)
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        withDependencies { (repo, xa) =>
          val results = for {
            - <- executeSqlScript(additionSQLScript)(using xa)
            randomPipelines <- List
              .fill(1000)(pipeline1.copy(id = UUID.randomUUID()))
              .pure[IO]
            inserts   <- randomPipelines.parTraverse(pipeline => repo.create(pipeline))
            pipelines <- repo.findAll(0, 1000)
          } yield pipelines
          results.asserting(_.size shouldBe 1000)
        }
      }
    }
  }
