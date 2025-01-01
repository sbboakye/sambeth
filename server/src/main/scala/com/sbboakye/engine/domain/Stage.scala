package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{PipelineId, StageConfiguration, StageId}
import doobie.Read
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.time.OffsetDateTime
import java.util.UUID

case class Stage(
    id: StageId,
    pipelineID: PipelineId,
    stageType: StageType,
    connectors: Seq[Connector],
    configuration: StageConfiguration,
    position: Int,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)

object Stage:
  given Read[Stage] =
    Read[
      (
          StageId,
          PipelineId,
          StageType,
          StageConfiguration,
          Int,
          OffsetDateTime,
          OffsetDateTime
      )
    ]
      .map {
        case (
              id,
              pipelineId,
              stageType,
              configuration,
              position,
              createdAt,
              updatedAt
            ) =>
          Stage(
            id,
            pipelineId,
            stageType,
            Seq.empty[Connector],
            configuration,
            position,
            createdAt,
            updatedAt
          )
      }
