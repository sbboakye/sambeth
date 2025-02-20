package com.sbboakye.engine.repositories.core

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, ConnectorId, StageId}
import com.sbboakye.engine.domain.{Connector, ConnectorType}
import doobie.{Meta, Read}
import doobie.postgres.*
import doobie.postgres.implicits.*
import io.circe.parser.*
import io.circe.syntax.*
import org.postgresql.util.PGobject

import java.time.OffsetDateTime

object DBFieldMappingsMeta:
  given Meta[Map[String, String]] = Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[Map[String, String]](obj => {
      decode[Map[String, String]](obj.getValue).getOrElse(Map.empty)
    })(map => {
      val pgObject = new PGobject()
      pgObject.setType("jsonb")
      pgObject.setValue(map.asJson.noSpaces)
      pgObject
    })

  given Read[Connector] = Read[
    (
        ConnectorId,
        StageId,
        String,
        ConnectorType,
        ConnectorConfiguration,
        OffsetDateTime,
        OffsetDateTime
    )
  ].map { case (id, stageId, name, connectorType, configuration, createdAt, updatedAt) =>
    Connector(id, stageId, name, connectorType, configuration, createdAt, updatedAt)
  }
