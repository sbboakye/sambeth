package com.sbboakye.engine.domain

import cats.data.NonEmptyList
import com.sbboakye.engine.domain.CustomTypes.{ConnectorId, PipelineId, StageConfiguration, StageId}
import doobie.{Get, Meta, Put, Read}
import doobie.postgres.circe.jsonb.implicits.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import org.postgresql.util.PGobject

import java.time.OffsetDateTime
import java.util.UUID

case class Stage(
    id: StageId,
    pipelineID: PipelineId,
    stageType: StageType,
    connectors: Seq[Connector],
    configuration: StageConfiguration,
    position: Int,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)

object Stage:
  given jsonMetaMap: Meta[Map[String, String]] = Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[Map[String, String]](obj =>
      decode[Map[String, String]](obj.getValue).getOrElse(Map.empty)
    )(map => {
      val pgObject = new PGobject()
      pgObject.setType("jsonb")
      pgObject.setValue(map.asJson.noSpaces)
      pgObject
    })
