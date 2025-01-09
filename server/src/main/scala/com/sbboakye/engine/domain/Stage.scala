package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{PipelineId, StageConfiguration, StageId}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.pipeline.PipelinesRepository
import doobie.Read
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
) {
  def getPipeline[F[_]](using repository: PipelinesRepository[F]): F[Option[Pipeline]] =
    repository.findById(pipelineID)
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

  def loadConnectors[F[_]](listOfIds: List[StageId])(using
      connectorRepository: ConnectorsRepository[F]
  ): F[Seq[Connector]] =
    connectorRepository.findAllByStageIds(listOfIds)
