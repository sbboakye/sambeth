package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorId, StageConfiguration, StageId}

import java.time.OffsetDateTime

case class Stage(
    id: StageId,
    stageType: StageType,
    connectors: List[Connector],
    configuration: StageConfiguration,
    position: Int,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
