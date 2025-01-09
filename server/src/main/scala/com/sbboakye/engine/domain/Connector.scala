package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId, StageId}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.stage.StagesRepository

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
      repository: StagesRepository[F],
      connectorRepository: ConnectorsRepository[F]
  ): F[Option[Stage]] =
    repository.findById(stageId)
}
