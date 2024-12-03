package com.sbboakye.engine.domain

import java.util.UUID

object CustomTypes {

  type PipelineId     = UUID
  type StageId        = UUID
  type ExecutionId    = UUID
  type ExecutionLogId = UUID
  type ConnectorId    = UUID
  type ScheduleId     = UUID

  type StageConfiguration     = Map[String, String]
  type ConnectorConfiguration = Map[String, String]

}
