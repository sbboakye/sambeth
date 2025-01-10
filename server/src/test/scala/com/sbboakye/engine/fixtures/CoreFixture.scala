package com.sbboakye.engine.fixtures

import cats.kernel.Eq
import cats.data.NonEmptyChain
import com.sbboakye.engine.domain.{
  Connector,
  DomainValidation,
  PipelineExecution,
  PipelineExecutionLog,
  ExecutionStatus,
  InvalidCronExpression,
  InvalidTimezone,
  Pipeline,
  Schedule,
  Stage
}
import com.sbboakye.engine.domain.ConnectorType.{API, Database}
import com.sbboakye.engine.domain.ExecutionStatus.{Failed, Running}
import com.sbboakye.engine.domain.LogLevel.Info
import com.sbboakye.engine.domain.StageType.Source
import com.sbboakye.engine.domain.PipelineStatus.Draft

import java.time.OffsetDateTime
import java.util.UUID

trait CoreFixture:
  val scheduleId: UUID            = UUID.fromString("66666666-6666-6666-6666-666666666661")
  val connector1Id: UUID          = UUID.randomUUID()
  val connector2Id: UUID          = UUID.randomUUID()
  val stageId1: UUID              = UUID.fromString("22222222-2222-2222-2222-222222222221")
  val stageId2: UUID              = UUID.fromString("22222222-2222-2222-2222-222222222222")
  val pipelineId1: UUID           = UUID.fromString("11111111-1111-1111-1111-111111111111")
  val pipelineId2: UUID           = UUID.fromString("11111111-1111-1111-1111-111111111112")
  val executionId1: UUID          = UUID.fromString("44444444-4444-4444-4444-444444444441")
  val executionId2: UUID          = UUID.fromString("44444444-4444-4444-4444-444444444442")
  val executionLogId: UUID        = UUID.fromString("55555555-5555-5555-5555-555555555551")
  val nonExistentId: UUID         = UUID.randomUUID()
  val updateConnectorName: String = "Updated name"
  val updateStageConfiguration: Map[String, String] = Map("source" -> "sink")
  val updatePipelineDescription: Option[String]     = Some("Updated Description")
  val updateExecutionStatus: ExecutionStatus        = Running

  val validCronExpression: String       = "0 0 12 * * ?"
  val validUpdateCronExpression: String = "0 0 11 * * ?"
  val validTimezone: String             = "UTC"
  val createValidSchedule: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(id = scheduleId, cronExpression = validCronExpression, timezone = validTimezone)
  val validSchedule: Schedule = createValidSchedule.toOption.get

  val connector1: Connector = Connector(
    connector1Id,
    stageId1,
    "test connector 1",
    Database,
    Map("db_name" -> "postgresql"),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val connector2: Connector = Connector(
    connector2Id,
    stageId1,
    "test connector 2",
    API,
    Map("url" -> "example.com"),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val stage1: Stage = Stage(
    stageId1,
    pipelineId1,
    Source,
    Seq(connector1, connector2),
    Map("some_config" -> "some value"),
    1,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val stage2: Stage = Stage(
    stageId2,
    pipelineId1,
    Source,
    Seq(connector2),
    Map("some_config" -> "some value"),
    2,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val pipeline1: Pipeline = Pipeline(
    pipelineId1,
    "pipeline 1",
    Some("A simple test pipeline 1"),
    Seq(stage1),
    Some(validSchedule.id),
    Draft,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val pipeline2: Pipeline = Pipeline(
    pipelineId2,
    "pipeline 2",
    Some("A simple test pipeline 2"),
    Seq(stage1),
    None,
    Draft,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val executionLog: PipelineExecutionLog = PipelineExecutionLog(
    executionLogId,
    executionId1,
    stageId1,
    OffsetDateTime.now(),
    "Some was written to the logs",
    Info,
    OffsetDateTime.now()
  )

  val execution: PipelineExecution = PipelineExecution(
    executionId1,
    pipelineId1,
    OffsetDateTime.now(),
    None,
    Running,
    Seq(executionLog),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  given Eq[DomainValidation] = Eq.fromUniversalEquals

  val invalidScheduleWithNullCronExpression: Schedule  = validSchedule.copy(cronExpression = null)
  val invalidScheduleWithEmptyCronExpression: Schedule = validSchedule.copy(cronExpression = "")
  val invalidScheduleWithNullTimezone: Schedule        = validSchedule.copy(timezone = null)
  val invalidScheduleWithEmptyTimezone: Schedule       = validSchedule.copy(timezone = "")

  val nonExistentScheduleId: UUID = UUID.randomUUID()

  val invalidCronExpression: String = "0 0 12 * *"
  val invalidTimezone: String       = "UTT"
  val createInvalidScheduleWithBothParametersInvalid
      : Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(cronExpression = invalidCronExpression, timezone = invalidTimezone)

  val createInvalidScheduleWithOnlyTimezoneValid
      : Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(cronExpression = invalidCronExpression, timezone = validTimezone)

  val createInvalidScheduleWithOnlyCronValid: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(cronExpression = validCronExpression, timezone = invalidTimezone)

  val allValidationsSet: Set[DomainValidation] = Set(InvalidCronExpression, InvalidTimezone)
