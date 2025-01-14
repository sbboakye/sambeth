package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.PipelineExecutionLog
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository
import doobie.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import com.sbboakye.engine.contexts.RepositoryContext.{
  NoHelper,
  pipelineExecutionLogsRepositorySetup
}
import org.scalatest.Assertion

import java.util.UUID

class PipelineExecutionLogsRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript1: String     = "pipelines.sql"
  val additionSQLScript2: String     = "stages.sql"
  val additionSQLScript3: String     = "executions.sql"

  "PipelineExecutionLogsRepository" - {
    "findAll" - {
      "should return an empty list when no execution logs exist" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, _) =>
          repo.findAll(0, 10).asserting(_ shouldBe empty)
        }
      }

      "should return a list of execution logs when execution log exist" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            -           <- executeSqlScript(additionSQLScript3)
            _           <- repo.create(executionLog)
            queryResult <- repo.findAll(0, 10)
          } yield queryResult
          result.asserting(_ should not be empty)
        }
      }
    }

    "findById" - {
      "should return None if the execution log does not exist" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.findById(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }

      "should return the correct execution log if the execution log exists" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            -           <- executeSqlScript(additionSQLScript3)
            uuid        <- repo.create(executionLog)
            queryResult <- repo.findById(uuid)
          } yield (queryResult, uuid)
          result.asserting((queryResult, uuid) => {
            queryResult.get.id shouldBe uuid
          })
        }
      }
    }

    "create" - {
      "should create a new execution log and return its id" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            -           <- executeSqlScript(additionSQLScript3)
            queryResult <- repo.create(executionLog)
          } yield queryResult
          result.asserting(_ should not be null)
        }
      }
    }

    "delete" - {
      "should delete an existing execution log" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -            <- executeSqlScript(additionSQLScript1)
            -            <- executeSqlScript(additionSQLScript2)
            -            <- executeSqlScript(additionSQLScript3)
            id           <- repo.create(executionLog)
            deleteResult <- repo.delete(id)
          } yield deleteResult
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if execution log does not exist" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, _) =>
          val result = repo.delete(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val results = for {
            - <- executeSqlScript(additionSQLScript1)
            - <- executeSqlScript(additionSQLScript2)
            - <- executeSqlScript(additionSQLScript3)
            randomExecutionLogs <- List
              .fill(10)(executionLog.copy(id = UUID.randomUUID()))
              .pure[IO]
            inserts <- randomExecutionLogs.parTraverse(log => repo.create(log))
            logs    <- repo.findAll(0, 20)
          } yield (inserts, logs)
          results.asserting(_._1.size shouldBe 10)
          results.asserting(_._2.size shouldBe 10)
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        withDependencies[PipelineExecutionLogsRepository, NoHelper, Assertion] { (repo, _, xa) =>
          given Transactor[IO] = xa
          val results = for {
            - <- executeSqlScript(additionSQLScript1)
            - <- executeSqlScript(additionSQLScript2)
            - <- executeSqlScript(additionSQLScript3)
            randomExecutionLogs <- List
              .fill(1000)(executionLog.copy(id = UUID.randomUUID()))
              .pure[IO]
            inserts <- randomExecutionLogs.parTraverse(log => repo.create(log))
            logs    <- repo.findAll(0, 1000)
          } yield logs
          results.asserting(_.size shouldBe 1000)
        }
      }
    }
  }
