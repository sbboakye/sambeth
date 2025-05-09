package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.syntax.all.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.fixtures.CoreFixture
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import doobie.*
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class ConnectorsRepositoryTests
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CoreSpec
    with CoreFixture:

  import repositoryContext.connectorsRepositorySetup
  override val initSqlString: String = "sql/postgres.sql"
  val additionSQLScript1: String     = "pipelines.sql"
  val additionSQLScript2: String     = "stages.sql"

  "ConnectorsRepository" - {
    "findAll" - {
      "should return an empty list when no connectors exist" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, _) =>
          repo.findAll(0, 10).asserting(_ shouldBe empty)
        }
      }

      "should return a list of connectors when connectors exist" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            _           <- repo.create(connector1)
            _           <- repo.create(connector2)
            queryResult <- repo.findAll(0, 10)
          } yield queryResult
          result.asserting(_ should not be empty)
        }
      }
    }

    "findById" - {
      "should return None if the connector does not exist" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, _) =>
          val result = repo.findById(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }

      "should return the correct connector if the connector exists" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            uuid        <- repo.create(connector1)
            queryResult <- repo.findById(uuid)
          } yield (queryResult, uuid)
          result.asserting((queryResult, uuid) => {
            queryResult.get.id shouldBe uuid
          })
        }
      }
    }

    "create" - {
      "should create a new connector and return its id" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            queryResult <- repo.create(connector1)
          } yield queryResult
          result.asserting(_ should not be null)
        }
      }
    }

    "update" - {
      "should update an existing connector" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -  <- executeSqlScript(additionSQLScript1)
            -  <- executeSqlScript(additionSQLScript2)
            id <- repo.create(connector1)
            updatedConnectorObject <- connector1
              .copy(name = updateConnectorName)
              .pure[IO]
            updatedConnector <- repo.update(id, updatedConnectorObject)
          } yield updatedConnector
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if connector does not exist" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            queryResult <- repo.update(nonExistentId, connector1)
          } yield queryResult
          result.asserting(_ shouldBe None)
        }
      }
    }

    "delete" - {
      "should delete an existing connector" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val result = for {
            -            <- executeSqlScript(additionSQLScript1)
            -            <- executeSqlScript(additionSQLScript2)
            id           <- repo.create(connector1)
            deleteResult <- repo.delete(id)
          } yield deleteResult
          result.asserting(_.get should be > 0)
        }
      }

      "should return None if connector does not exist" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, _) =>
          val result = repo.delete(nonExistentId)
          result.asserting(_ shouldBe None)
        }
      }
    }

    "Edge Cases: Concurrent Transactions" - {
      "should handle concurrent inserts without data loss" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val results = for {
            -                <- executeSqlScript(additionSQLScript1)
            -                <- executeSqlScript(additionSQLScript2)
            randomConnectors <- List.fill(10)(connector1.copy(id = UUID.randomUUID())).pure[IO]
            inserts <- randomConnectors.parTraverse(randomConnector => repo.create(randomConnector))
            connectors <- repo.findAll(0, 20)
          } yield (inserts, connectors)
          results.asserting(_._1.size shouldBe 10)
          results.asserting(_._2.size shouldBe 10)
        }
      }

      "should handle concurrent updates correctly" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val results = for {
            -           <- executeSqlScript(additionSQLScript1)
            -           <- executeSqlScript(additionSQLScript2)
            connectorId <- repo.create(connector1)
            connectors <- List
              .fill(10)(
                connector1.copy(id = connectorId, name = updateConnectorName)
              )
              .pure[IO]
            updates <- connectors.parTraverse(connector => repo.update(connectorId, connector))
            fetchedConnector <- repo.findById(connectorId)
          } yield (updates, fetchedConnector)
          results.asserting(_._1.flatMap(_.toList).reduceLeftOption(_ + _) shouldBe Option(10))
          results.asserting(_._2.get.name shouldBe updateConnectorName)
        }
      }
    }

    "Edge Cases: Large Dataset" - {
      "should handle large number of records in findAll" in {
        withDependencies[ConnectorsRepository, Assertion] { (repo, xa) =>
          given Transactor[IO] = xa
          val results = for {
            - <- executeSqlScript(additionSQLScript1)
            - <- executeSqlScript(additionSQLScript2)
            randomConnectors <- List
              .fill(1000)(connector1.copy(id = UUID.randomUUID()))
              .pure[IO]
            inserts    <- randomConnectors.parTraverse(connector => repo.create(connector))
            connectors <- repo.findAll(0, 1000)
          } yield connectors
          results.asserting(_.size shouldBe 1000)
        }
      }
    }
  }
