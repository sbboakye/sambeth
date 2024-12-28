package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.{Connector, Stage}

import java.time.OffsetDateTime
import java.util.UUID

case class StageWithConnectors(
    stage: (UUID, UUID, StageType, Map[String, String], Int, OffsetDateTime, OffsetDateTime),
    connectors: Seq[Connector]
):
  def convertToStage: Stage =
    Stage(
      id = stage._1,
      pipelineID = stage._2,
      stageType = stage._3,
      connectors = connectors,
      configuration = stage._4,
      position = stage._5,
      createdAt = stage._6,
      updatedAt = stage._7
    )
