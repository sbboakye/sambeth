package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId}

case class Connector(
    id: ConnectorId,
    name: String,
    connectorType: ConnectorType,
    configuration: ConnectorConfiguration
)
