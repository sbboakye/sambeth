package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.PipelineId
import doobie.Read
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.time.OffsetDateTime

case class Pipeline(
    id: PipelineId,
    name: String,
    description: Option[String],
    stages: Seq[Stage],
    schedule: Option[Schedule],
    status: PipelineStatus,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)

object Pipeline:
  given Read[Pipeline] = Read[
    (
        PipelineId,
        String,
        Option[String],
        Option[Schedule],
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
            schedule,
            status,
            createdAt,
            updatedAt
          ) =>
        Pipeline(
          id,
          name,
          description,
          Seq.empty[Stage],
          schedule,
          status,
          createdAt,
          updatedAt
        )
    }
