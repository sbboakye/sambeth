package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.Stage
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class StagesRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript: String      = "pipelines.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  given Core[IO, Stage] with {}

  "StagesRepository" - {
    "findAll" - {
      "should return an empty list when no stages exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = repo.findAll(0, 10)
            result.asserting(_ shouldBe empty)
          }
        }
      }

      "should return a list of stages when stages exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)
              _           <- repo.create(stage1)
              _           <- repo.create(stage2)
              queryResult <- repo.findAll(0, 10)
            } yield queryResult
            result.asserting(_ should not be empty)
          }
        }
      }
    }

    "findById" - {
      "should return None if the stage does not exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = repo.findById(nonExistentId)
            result.asserting(_ shouldBe None)
          }
        }
      }

      "should return the correct stage if the stage exists" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)
              uuid        <- repo.create(stage1)
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
      "should create a new stage and return its id" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)
              queryResult <- repo.create(stage1)
            } yield queryResult
            result.asserting(_ should not be null)
          }
        }
      }
    }

    "update" - {
      "should update an existing stage" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -  <- executeSqlScript(additionSQLScript)
              id <- repo.create(stage1)
              updatedStageObject <- stage1
                .copy(configuration = updateStageConfiguration)
                .pure[IO]
              updatedStage <- repo.update(id, updatedStageObject)
            } yield updatedStage
            result.asserting(_.get should be > 0)
          }
        }
      }

      "should return None if stage does not exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)
              queryResult <- repo.update(nonExistentId, stage1)
            } yield queryResult
            result.asserting(_ shouldBe None)
          }
        }
      }
    }

    "delete" - {
      "should delete an existing stage" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = for {
              -            <- executeSqlScript(additionSQLScript)
              id           <- repo.create(stage1)
              deleteResult <- repo.delete(id)
            } yield deleteResult
            result.asserting(_.get should be > 0)
          }
        }
      }

      "should return None if stage does not exist" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val result = repo.delete(nonExistentId)
            result.asserting(_ shouldBe None)
          }
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa

          StagesRepository[IO].use { repo =>
            val results = for {
              -            <- executeSqlScript(additionSQLScript)
              randomStages <- List.fill(10)(stage1.copy(id = UUID.randomUUID())).pure[IO]
              inserts      <- randomStages.parTraverse(randomStage => repo.create(randomStage))
              stages       <- repo.findAll(0, 20)
            } yield (inserts, stages)
            results.asserting(_._1.size shouldBe 10)
            results.asserting(_._2.size shouldBe 10)
          }
        }
      }

      "should handle concurrent updates correctly" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val results = for {
              -       <- executeSqlScript(additionSQLScript)
              stageId <- repo.create(stage1)
              stages <- List
                .fill(10)(
                  stage1.copy(id = stageId, configuration = updateStageConfiguration)
                )
                .pure[IO]
              updates      <- stages.parTraverse(stage => repo.update(stageId, stage))
              fetchedStage <- repo.findById(stageId)
            } yield (updates, fetchedStage)
            results.asserting(_._1.flatMap(_.toList).reduceLeftOption(_ + _) shouldBe Option(10))
            results.asserting(_._2.get.configuration shouldBe updateStageConfiguration)
          }
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        coreSpecTransactor.use { xa =>
          given transactor: Transactor[IO] = xa
          StagesRepository[IO].use { repo =>
            val results = for {
              - <- executeSqlScript(additionSQLScript)
              randomStages <- List
                .fill(1000)(stage1.copy(id = UUID.randomUUID()))
                .pure[IO]
              inserts <- randomStages.parTraverse(stage => repo.create(stage))
              stages  <- repo.findAll(0, 1000)
            } yield stages
            results.asserting(_.size shouldBe 1000)
          }
        }
      }
    }
  }
