package com.sbboakye.engine.fixtures

import cats.data.NonEmptyChain
import com.sbboakye.engine.domain.{Connector, DomainValidation, Pipeline, Schedule, Stage}
import com.sbboakye.engine.domain.ConnectorType.{API, Database}
import com.sbboakye.engine.domain.StageType.Source
import com.sbboakye.engine.domain.PipelineStatus.Draft

import java.time.OffsetDateTime
import java.util.UUID

trait ConnectorFixture:
  val connector1Id: UUID           = UUID.randomUUID()
  val connector2Id: UUID           = UUID.randomUUID()
  val stageId1: UUID               = UUID.fromString("22222222-2222-2222-2222-222222222221")
  val stageId2: UUID               = UUID.fromString("22222222-2222-2222-2222-222222222222")
  val pipelineId: UUID             = UUID.fromString("11111111-1111-1111-1111-111111111111")
  val nonExistentConnectorId: UUID = UUID.randomUUID()
  val nonExistentStageId: UUID     = UUID.randomUUID()
  val updateConnectorName: String  = "Updated name"
  val updateStageConfiguration: Map[String, String] = Map("source" -> "sink")

  val validCronExpression: String       = "0 0 12 * * ?"
  val validUpdateCronExpression: String = "0 0 11 * * ?"
  val validTimezone: String             = "UTC"
  val createValidSchedule: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(validCronExpression, validTimezone)
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
    pipelineId,
    Source,
    Seq(connector1, connector2),
    Map("some_config" -> "some value"),
    1,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val stage2: Stage = Stage(
    stageId2,
    pipelineId,
    Source,
    Seq(connector2),
    Map("some_config" -> "some value"),
    2,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val pipeline: Pipeline = Pipeline(
    pipelineId,
    "pipeline",
    Some("A simple test pipeline"),
    Seq(stage1),
    Some(validSchedule),
    Draft,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )
