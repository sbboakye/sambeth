package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{PipelineId, StageConfiguration, StageId}
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository
import doobie.Read
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.OffsetDateTime

case class Stage(
    id: StageId,
    pipelineId: PipelineId,
    stageType: StageType,
    connectors: Seq[Connector],
    configuration: StageConfiguration,
    position: Int,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def getPipelineMetadata[F[_]](using
      repository: PipelinesRepository[F]
  ): F[Option[PipelineMetadata]] =
    repository.findMetadataById(pipelineId)
}

object Stage:
  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

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

  given Encoder[Stage] = deriveEncoder[Stage]
  given Decoder[Stage] = deriveDecoder[Stage]
