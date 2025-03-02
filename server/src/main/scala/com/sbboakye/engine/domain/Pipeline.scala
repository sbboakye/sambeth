package com.sbboakye.engine.domain

import cats.*
import cats.syntax.all.*
import cats.effect.kernel.MonadCancelThrow
import com.sbboakye.engine.domain.CustomTypes.{PipelineId, ScheduleId}
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import doobie.Read
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.OffsetDateTime

case class Pipeline(
    id: PipelineId,
    name: String,
    description: Option[String],
    stages: Seq[Stage],
    scheduleId: Option[ScheduleId],
    status: PipelineStatus,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def getSchedule[F[_]: MonadCancelThrow](using
      repository: SchedulesRepository[F]
  ): F[Either[String, Schedule]] =
    scheduleId.fold(MonadCancelThrow[F].pure(Left("No schedule is associated with this pipeline")))(
      id =>
        repository.findById(id).map {
          case Some(schedule) => Right(schedule)
          case None           => Left(s"Schedule with ID $id not found")
        }
    )
}

object Pipeline:
  given Read[Pipeline] = Read[
    (
        PipelineId,
        String,
        Option[String],
        Option[ScheduleId],
        PipelineStatus,
        OffsetDateTime,
        OffsetDateTime
    )
  ]
    .map {
      case (
            id,
            name,
            description,
            scheduleId,
            status,
            createdAt,
            updatedAt
          ) =>
        Pipeline(
          id,
          name,
          description,
          Seq.empty[Stage],
          scheduleId,
          status,
          createdAt,
          updatedAt
        )
    }

  given Encoder[Pipeline] = deriveEncoder[Pipeline]
  given Decoder[Pipeline] = deriveDecoder[Pipeline]
