package com.sbboakye.engine.repositories.core

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, StageConfiguration}
import doobie.postgres.circe.json.implicits.*
import doobie.postgres.circe.jsonb.implicits.*
import doobie.{Get, Meta, Put, Read}
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.postgresql.util.PGobject

object ConnectorConfigurationJsonMeta:
  given Meta[ConnectorConfiguration] = Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[ConnectorConfiguration](obj =>
      decode[ConnectorConfiguration](obj.getValue).getOrElse(Map.empty)
    )(map => {
      val pgObject = new PGobject()
      pgObject.setType("jsonb")
      pgObject.setValue(map.asJson.noSpaces)
      pgObject
    })
