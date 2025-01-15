package com.sbboakye.engine.routes

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.services.ScheduleService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import io.circe.generic.auto.*
import org.http4s.server.Router

class ScheduleRoutes[F[_]: Concurrent] private (scheduleService: ScheduleService[F]):
  private val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl.*

  private val prefix = "/"

  private val listRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "schedules" =>
    scheduleService.findAll.flatMap(Ok(_))
  }

  val routes: HttpRoutes[F] = Router(
    prefix -> (
      listRoute
    )
  )

object ScheduleRoutes:
  def apply[F[_]: Concurrent](scheduleService: ScheduleService[F]): ScheduleRoutes[F] =
    new ScheduleRoutes[F](scheduleService)
