package com.sbboakye.engine.routes

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.domain.Pipeline
import com.sbboakye.engine.services.{ApiResponse, PipelineService}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.server.Router

class PipelineRoutes[F[_]: Concurrent] private (pipelineService: PipelineService[F]):
  private val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl.*

  private val entity = "pipelines"

  private val prefix = s"/$entity"

  private val listAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    pipelineService.findAll.flatMap(pipelines => Ok(ApiResponse.Success(pipelines)))
  }

  private val detailAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(pipelineId) =>
      pipelineService
        .findById(pipelineId)
        .flatMap(
          _.fold(NotFound(ApiResponse.Error("Pipeline not found")))(pipeline =>
            Ok(ApiResponse.Success(List(pipeline)))
          )
        )
  }

  private val createAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      req
        .as[Pipeline]
        .flatMap { pipeline =>
          pipelineService.create(pipeline).flatMap { pipelineId =>
            Created(ApiResponse.Success(List(Map("id" -> pipelineId))))
          }
        }
  }

  private val updateAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / "update" / UUIDVar(pipelineId) =>
      req.as[Pipeline].flatMap { pipeline =>
        pipelineService.update(pipelineId, pipeline).flatMap { numberOfUpdates =>
          Ok(ApiResponse.Success(numberOfUpdates))
        }
      }
  }

  private val deleteAPIRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / "delete" / UUIDVar(pipelineId) =>
      pipelineService
        .delete(pipelineId)
        .flatMap(
          _.fold(NotFound(ApiResponse.Error("Pipeline not found")))(numberOfDeletes =>
            Ok(ApiResponse.Success(numberOfDeletes))
          )
        )
  }

  val routes: HttpRoutes[F] = Router(
    prefix -> (
      listAPIRoute <+> detailAPIRoute <+> createAPIRoute <+> updateAPIRoute <+> deleteAPIRoute
    )
  )

object PipelineRoutes:
  def apply[F[_]](pipelineService: PipelineService[F])(using
      F: Concurrent[F]
  ): Resource[F, PipelineRoutes[F]] =
    Resource.eval(F.pure(new PipelineRoutes[F](pipelineService)))
