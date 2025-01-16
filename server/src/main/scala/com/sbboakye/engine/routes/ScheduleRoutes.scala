package com.sbboakye.engine.routes

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.domain.{Schedule, ScheduleCreate}
import com.sbboakye.engine.services.{ApiResponse, ScheduleService}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import org.http4s.server.Router

class ScheduleRoutes[F[_]: Concurrent] private (scheduleService: ScheduleService[F]):
  private val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl.*

  private val prefix = "/"

  private val entity = "schedules"

  private val listAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / entity =>
    scheduleService.findAll.flatMap(schedules => Ok(ApiResponse.Success(schedules)))
  }

  private val detailAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / entity / UUIDVar(scheduleId) =>
      scheduleService
        .findById(scheduleId)
        .flatMap(_.fold(NotFound())(schedule => Ok(ApiResponse.Success(schedule))))
  }

  private val createAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / entity / "create" =>
      req.as[ScheduleCreate].flatMap { scheduleCreate =>
        Schedule.create(
          cronExpression = scheduleCreate.cronExpression,
          timezone = scheduleCreate.timezone
        ) match {
          case Right(schedule) =>
            scheduleService.create(schedule).flatMap { scheduleId =>
              Created(ApiResponse.Success(scheduleId))
            }
          case Left(validationErrors) =>
            val errorMessage = validationErrors.toList.map(_.errorMessage).mkString(", ")
            BadRequest(ApiResponse.Error(errorMessage))
        }

      }
  }

  private val updateAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / entity / "update" / UUIDVar(scheduleId) =>
      req.as[ScheduleCreate].flatMap { scheduleCreate =>
        Schedule.create(
          cronExpression = scheduleCreate.cronExpression,
          timezone = scheduleCreate.timezone
        ) match {
          case Right(schedule) =>
            scheduleService
              .update(scheduleId, schedule)
              .flatMap(
                _.fold(NotFound())(numberOfUpdates => Ok(ApiResponse.Success(numberOfUpdates)))
              )
          case Left(validationErrors) =>
            val errorMessage = validationErrors.toList.map(_.errorMessage).mkString(", ")
            BadRequest(ApiResponse.Error(errorMessage))
        }
      }
  }

  private val deleteAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / entity / "delete" / UUIDVar(scheduleId) =>
      scheduleService
        .delete(scheduleId)
        .flatMap(_.fold(NotFound())(numberOfDeletes => Ok(ApiResponse.Success(numberOfDeletes))))
  }

  val routes: HttpRoutes[F] = Router(
    prefix -> (
      listAPIRoute <+> detailAPIRoute <+> createAPIRoute <+> updateAPIRoute <+> deleteAPIRoute
    )
  )

object ScheduleRoutes:
  def apply[F[_]: Concurrent](scheduleService: ScheduleService[F]): ScheduleRoutes[F] =
    new ScheduleRoutes[F](scheduleService)
