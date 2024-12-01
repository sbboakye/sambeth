package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{StageConfiguration, StageId}

case class Stage(
    id: StageId,
    stageType: StageType,
    configuration: StageConfiguration,
    order: Int
)
