package com.sbboakye.engine.domain

import java.time.OffsetDateTime
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
  type StageConnectorJoined = Tuple14[
    UUID,
    UUID,
    StageType,
    Map[String, String],
    Int,
    OffsetDateTime,
    OffsetDateTime,
    UUID,
    UUID,
    String,
    ConnectorType,
    Map[String, String],
    OffsetDateTime,
    OffsetDateTime
  ]

}
