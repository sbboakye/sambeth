package com.sbboakye.engine.repositories

import cats.*
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import cats.kernel.Eq
import com.dimafeng.testcontainers.{JdbcDatabaseContainer, PostgreSQLContainer}
import com.sbboakye.engine.contexts.{RepositoryContext, RepositorySetup}
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.circe.Json
import org.http4s.{EntityDecoder, HttpRoutes, Method, Request, Response, Status, Uri}
import org.http4s.circe.*
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CoreSpec:
  val initSqlString: String

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val repositoryContext: RepositoryContext[IO] = RepositoryContext[IO]

  val postgres: Resource[IO, PostgreSQLContainer] = {
    val acquire = IO {
      val container = PostgreSQLContainer.Def(
        dockerImageName = DockerImageName.parse("postgres"),
        databaseName = "test-database-container",
        username = "scala",
        password = "scala",
        commonJdbcParams =
          JdbcDatabaseContainer.CommonParams(initScriptPath = Option(initSqlString))
      )
      container.start()
    }
    val release = (container: PostgreSQLContainer) => IO(container.close())
    Resource.make(acquire)(release)
  }

  val coreSpecTransactor: Resource[IO, Transactor[IO]] = for {
    db <- postgres
    ce <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.jdbcUrl,
      db.username,
      db.password,
      ce
    )
  } yield xa

  def executeSqlScript(scriptPath: String)(using xa: Transactor[IO]): IO[Unit] =
    val script     = scala.io.Source.fromResource(scriptPath).mkString
    val statements = script.split(";").filter(_.trim.nonEmpty)
    statements.toList.traverse_ { sql =>
      Fragment.const(sql.trim).update.run.transact(xa)
    }

  def withDependencies[Repo[_[_]], H[_[_]], T](
      test: (Repo[IO], H[IO], Transactor[IO]) => IO[T]
  )(using setup: RepositorySetup[Repo, H, IO]): IO[T] = {
    coreSpecTransactor.use { xa =>
      setup.use(xa) { (repo, helper, xa) =>
        test(repo, helper, xa)
      }
    }
  }

  def extractRelevantFields(json: Json, fieldsToExtract: List[String]): IO[Json] = {
    IO.pure(
      json.hcursor
        .downField("data")
        .withFocus { dataArray =>
          dataArray.mapArray { array =>
            array.map { obj =>
              Json.obj(
                fieldsToExtract.flatMap { field =>
                  obj.hcursor.downField(field).focus.map(value => field -> value)
                }*
              )
            }
          }
        }
        .top
        .getOrElse(Json.Null)
    )
  }

  def check(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[Json],
      fieldsToCompare: List[String],
      onlyStatus: Boolean = false,
      checkErrorResponse: Boolean = false
  )(using
      ev: EntityDecoder[IO, Json]
  ): IO[Boolean] =
    given Eq[IO[Vector[Byte]]] = Eq.fromUniversalEquals

    def logResult[T](label: String, result: T): IO[Unit] = logger.info(s"$label: $result")

    def processBodyCheck(actualResponse: Response[IO], expectedBody: Option[Json]): IO[Boolean] =
      expectedBody match {
        case None =>
          for {
            isEmpty <- actualResponse.body.compile.toVector.map(_.isEmpty)
            _       <- logger.info(s"Body is empty: $isEmpty")
          } yield isEmpty
        case Some(expectedJson) =>
          for {
            actualJson <- actualResponse.as[Json]
            _          <- logger.info(s"Actual response: $actualJson")
            actualRelevant <-
              if checkErrorResponse then IO.pure(actualJson)
              else extractRelevantFields(actualJson, fieldsToCompare)
            _ <- logger.info(s"Actual: $actualRelevant")
            expectedRelevant <-
              if checkErrorResponse then IO.pure(expectedJson)
              else extractRelevantFields(expectedJson, fieldsToCompare)
            _ <- logger.info(s"Expected: $expectedRelevant")
            bodyMatches = actualRelevant == expectedRelevant
            _ <- logger.info(s"Body matches: $bodyMatches")
          } yield bodyMatches
      }

    for {
      result <- actual.attempt
      isSuccess <- result match {
        case Right(actualResponse) =>
          for {
            _ <- logger.info(s"Received response with status: ${actualResponse.status}")
            statusCheck = actualResponse.status == expectedStatus
            _ <- logger.info(
              s"Status check: expected $expectedStatus, got ${actualResponse.status}"
            )
            bodyCheck <-
              if (onlyStatus) IO.pure(true) else processBodyCheck(actualResponse, expectedBody)
            _ <- logger.info(s"Final result: ${statusCheck && bodyCheck}")
          } yield statusCheck && bodyCheck
        case Left(e) =>
          logger.error(s"No route matched for: ${e.getMessage}").as(false)
      }
    } yield isSuccess

  def testAPIEndpoints(
      httpMethod: Method,
      httpStatus: Status,
      endpoint: String,
      expectedJson: Option[Json] = None,
      maybeBody: Option[Json] = None,
      fieldsToCompare: List[String] = List.empty,
      onlyStatus: Boolean = false,
      checkErrorResponse: Boolean = false
  )(getRoutes: Transactor[IO] => IO[HttpRoutes[IO]]): IO[Boolean] =
    coreSpecTransactor.use { xa =>
      val router = getRoutes(xa)
      router.flatMap { routes =>
        val baseRequest = Request[IO](httpMethod, Uri.unsafeFromString(endpoint))
        val request = httpMethod match {
          case Method.POST | Method.PUT =>
            maybeBody match {
              case Some(body) => baseRequest.withEntity(body)
              case None       => baseRequest
            }
          case _ => baseRequest
        }
        val response = routes.run(request).value.map(_.get)
        check(
          actual = response,
          expectedStatus = httpStatus,
          expectedBody = expectedJson,
          fieldsToCompare = fieldsToCompare,
          onlyStatus = onlyStatus,
          checkErrorResponse = checkErrorResponse
        )
      }
    }
