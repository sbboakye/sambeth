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

  case class StageConnectorJoined(
      // Stages table fields
      stageId: UUID,
      pipelineId: UUID,
      stageType: StageType,
      stageConfiguration: StageConfiguration,
      stagePosition: Int,
      stageCreatedAt: OffsetDateTime,
      stageUpdatedAt: OffsetDateTime,

      // Connectors table fields
      connectorId: Option[UUID],                              // connector_id
      connectorStageId: Option[UUID],                         // connector_stage_id
      connectorName: Option[String],                          // connector_name
      connectorType: Option[ConnectorType],                   // connector_type
      connectorConfiguration: Option[ConnectorConfiguration], // connector_configuration
      connectorCreatedAt: Option[OffsetDateTime],             // connector_created_at
      connectorUpdatedAt: Option[OffsetDateTime]              // connector_updated_at
  )

  case class PipelineScheduleStageConnectorJoined(
      // Pipelines table
      pipelineId: UUID,
      pipelineName: String,
      pipelineDescription: Option[String],
      pipelineScheduleId: Option[UUID],
      pipelineStatus: PipelineStatus,
      pipelineCreatedAt: OffsetDateTime,
      pipelineUpdatedAt: OffsetDateTime,

      // Stages table
      stageId: Option[UUID],
      stagePipelineId: Option[UUID],
      stageType: Option[StageType],
      stageConfiguration: Option[StageConfiguration],
      stagePosition: Option[Int],
      stageCreatedAt: Option[OffsetDateTime],
      stageUpdatedAt: Option[OffsetDateTime],

      // Connectors table
      connectorId: Option[UUID],
      connectorStageId: Option[UUID],
      connectorName: Option[String],
      connectorType: Option[ConnectorType],
      connectorConfiguration: Option[ConnectorConfiguration],
      connectorCreatedAt: Option[OffsetDateTime],
      connectorUpdatedAt: Option[OffsetDateTime],

      // Schedules table
      scheduleId: Option[UUID],
      scheduleCronExpression: Option[String],
      scheduleTimezone: Option[String],
      scheduleCreatedAt: Option[OffsetDateTime],
      scheduleUpdatedAt: Option[OffsetDateTime]
  )

  case class ExecutionPipelineExecutionLogJoined(
      // Executions table fields
      executionId: UUID,
      executionPipelineId: UUID,
      executionStartTime: OffsetDateTime,
      executionEndTime: Option[OffsetDateTime],
      executionStatus: ExecutionStatus,
      executionCreatedAt: OffsetDateTime,
      executionUpdatedAt: OffsetDateTime,

      // Execution Logs table fields
      executionLogId: UUID,
      logStageId: UUID,
      logTimestamp: OffsetDateTime,
      logMessage: String,
      logLevel: LogLevel,
      logCreatedAt: OffsetDateTime,

      // Pipelines table fields
      pipelineId: UUID,
      pipelineName: String,
      pipelineDescription: Option[String],
      pipelineScheduleId: Option[UUID],
      pipelineStatus: PipelineStatus,
      pipelineCreatedAt: OffsetDateTime,
      pipelineUpdatedAt: OffsetDateTime
  )

}
