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
    Option[UUID],                // connector_id
    Option[UUID],                // connector_stage_id
    Option[String],              // connector_name
    Option[ConnectorType],       // connector_type
    Option[Map[String, String]], // connector_configuration
    Option[OffsetDateTime],      // connector_created_at
    Option[OffsetDateTime]       // connector_updated_at
  ]

}
