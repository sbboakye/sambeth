package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId}

import java.time.OffsetDateTime

case class Connector(
    id: ConnectorId,
    name: String,
    connectorType: ConnectorType,
    configuration: ConnectorConfiguration,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
