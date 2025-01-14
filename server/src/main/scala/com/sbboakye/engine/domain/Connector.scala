package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId, StageId}
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import com.sbboakye.engine.repositories.stage.{ConnectorsHelper, StagesRepository}
import doobie.postgres.implicits.*

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
  def getStage[F[_]](helper: ConnectorsHelper[F])(using
      repository: StagesRepository[F]
  ): F[Option[Stage]] =
    repository.findById(stageId, helper)
}
