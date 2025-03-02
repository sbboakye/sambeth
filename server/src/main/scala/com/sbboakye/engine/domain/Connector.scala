package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId, StageId}
import com.sbboakye.engine.repositories.stage.StagesRepository
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.OffsetDateTime

case class Connector(
    id: ConnectorId,
    stageId: StageId,
    name: String,
    connectorType: ConnectorType,
    configuration: ConnectorConfiguration,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def getStage[F[_]](using
      repository: StagesRepository[F]
  ): F[Option[Stage]] =
    repository.findById(stageId)
}

object Connector:
  given Encoder[Connector] = deriveEncoder[Connector]
  given Decoder[Connector] = deriveDecoder[Connector]
