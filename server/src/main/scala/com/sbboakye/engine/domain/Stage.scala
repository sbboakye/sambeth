package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ConnectorId, StageConfiguration, StageId}

case class Stage(
    id: StageId,
    stageType: StageType,
    connectorIds: List[ConnectorId],
    configuration: StageConfiguration,
    order: Int
)
