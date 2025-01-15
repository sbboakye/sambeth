package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.PipelineExecution
import com.sbboakye.engine.contexts.RepositoryContext.NoHelper
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.execution.{
  PipelineExecutionLogsHelper,
  PipelineExecutionsRepository
}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Assertion

import java.util.UUID

class PipelineExecutionsRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  import repositoryContext.pipelineExecutionsRepositorySetup
  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript: String      = "pipelines.sql"

  "ExecutionsRepository" - {
    "findAll" - {
      "should return an empty list when no executions exist" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, _) =>
            repo.findAll(0, 10, helper).asserting(_ shouldBe empty)
        }
      }

      "should return a list of executions when executions exist" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, xa) =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)(using xa)
              _           <- repo.create(execution)
              queryResult <- repo.findAll(0, 10, helper)
            } yield queryResult
            result.asserting(_ should not be empty)
        }
      }
    }

    "findById" - {
      "should return None if the execution does not exist" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, _) =>
            repo.findById(nonExistentId, helper).asserting(_ shouldBe None)
        }
      }

      "should return the correct execution if the execution exists" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, xa) =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)(using xa)
              uuid        <- repo.create(execution)
              queryResult <- repo.findById(uuid, helper)
            } yield (queryResult, uuid)
            result.asserting((queryResult, uuid) => {
              queryResult.get.id shouldBe uuid
            })
        }
      }
    }

    "create" - {
      "should create a new execution and return its id" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, _, xa) =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)(using xa)
              queryResult <- repo.create(execution)
            } yield queryResult
            result.asserting(_ should not be null)
        }
      }
    }

    "update" - {
      "should update an existing execution" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, _, xa) =>
            val result = for {
              -  <- executeSqlScript(additionSQLScript)(using xa)
              id <- repo.create(execution)
              updatedStageObject <- execution
                .copy(status = updateExecutionStatus)
                .pure[IO]
              updatedStage <- repo.update(id, updatedStageObject)
            } yield updatedStage
            result.asserting(_.get should be > 0)
        }
      }

      "should return None if execution does not exist" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, _, xa) =>
            val result = for {
              -           <- executeSqlScript(additionSQLScript)(using xa)
              queryResult <- repo.update(nonExistentId, execution)
            } yield queryResult
            result.asserting(_ shouldBe None)
        }
      }
    }

    "delete" - {
      "should delete an existing execution" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, _, xa) =>
            val result = for {
              -            <- executeSqlScript(additionSQLScript)(using xa)
              id           <- repo.create(execution)
              deleteResult <- repo.delete(id)
            } yield deleteResult
            result.asserting(_.get should be > 0)
        }
      }

      "should return None if execution does not exist" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, _, _) =>
            repo.delete(nonExistentId).asserting(_ shouldBe None)
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, xa) =>
            val results = for {
              -                <- executeSqlScript(additionSQLScript)(using xa)
              randomExecutions <- List.fill(10)(execution.copy(id = UUID.randomUUID())).pure[IO]
              inserts <- randomExecutions.parTraverse(randomExecution =>
                repo.create(randomExecution)
              )
              executions <- repo.findAll(0, 20, helper)
            } yield (inserts, executions)
            results.asserting(_._1.size shouldBe 10)
            results.asserting(_._2.size shouldBe 10)
        }
      }

      "should handle concurrent updates correctly" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, xa) =>
            val results = for {
              -           <- executeSqlScript(additionSQLScript)(using xa)
              executionId <- repo.create(execution)
              executions <- List
                .fill(10)(
                  execution.copy(id = executionId, status = updateExecutionStatus)
                )
                .pure[IO]
              updates <- executions.parTraverse(execution => repo.update(executionId, execution))
              fetchedExecution <- repo.findById(executionId, helper)
            } yield (updates, fetchedExecution)
            results.asserting(_._1.flatMap(_.toList).reduceLeftOption(_ + _) shouldBe Option(10))
            results.asserting(_._2.get.status shouldBe updateExecutionStatus)
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        withDependencies[PipelineExecutionsRepository, PipelineExecutionLogsHelper, Assertion] {
          (repo, helper, xa) =>
            val result = for {
              - <- executeSqlScript(additionSQLScript)(using xa)
              randomExecutions <- List
                .fill(1000)(execution.copy(id = UUID.randomUUID()))
                .pure[IO]
              inserts    <- randomExecutions.parTraverse(execution => repo.create(execution))
              executions <- repo.findAll(0, 1000, helper)
            } yield executions
            result.asserting(_.size shouldBe 1000)
        }
      }
    }
  }
