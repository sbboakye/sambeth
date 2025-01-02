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
  val stageId: UUID                = UUID.fromString("22222222-2222-2222-2222-222222222221")
  val pipelineId: UUID             = UUID.randomUUID()
  val nonExistentConnectorId: UUID = UUID.randomUUID()
  val updateConnectorName: String  = "Updated name"

  val validCronExpression: String       = "0 0 12 * * ?"
  val validUpdateCronExpression: String = "0 0 11 * * ?"
  val validTimezone: String             = "UTC"
  val createValidSchedule: Either[NonEmptyChain[DomainValidation], Schedule] =
    Schedule.create(validCronExpression, validTimezone)
  val validSchedule: Schedule = createValidSchedule.toOption.get

  val connector1: Connector = Connector(
    connector1Id,
    stageId,
    "test connector 1",
    Database,
    Map("db_name" -> "postgresql"),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val connector2: Connector = Connector(
    connector2Id,
    stageId,
    "test connector 2",
    API,
    Map("url" -> "example.com"),
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val stage: Stage = Stage(
    stageId,
    pipelineId,
    Source,
    Seq(connector1, connector2),
    Map("some_config" -> "some value"),
    1,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )

  val pipeline: Pipeline = Pipeline(
    pipelineId,
    "pipeline",
    Some("A simple test pipeline"),
    Seq(stage),
    Some(validSchedule),
    Draft,
    OffsetDateTime.now(),
    OffsetDateTime.now()
  )
