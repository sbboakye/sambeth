package com.sbboakye.engine.domain

import cats.data.NonEmptyList
import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId, StageId}

import java.time.OffsetDateTime

case class Connector(
    id: ConnectorId,
    stageId: StageId,
    name: String,
    connectorType: ConnectorType,
    configuration: ConnectorConfiguration,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
