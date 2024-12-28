package com.sbboakye.engine.repositories.core

import com.sbboakye.engine.domain.CustomTypes.{ConnectorConfiguration, StageConfiguration}
import doobie.postgres.circe.json.implicits.*
import doobie.postgres.circe.jsonb.implicits.*
import doobie.{Get, Meta, Put, Read}
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.postgresql.util.PGobject

object StageConfigurationJsonMeta:

  given Meta[StageConfiguration] = Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[StageConfiguration](obj =>
      println(s"Raw JSON: ${obj.getValue}")
      decode[StageConfiguration](obj.getValue).getOrElse(Map.empty)
    )(map => {
      val pgObject = new PGobject()
      pgObject.setType("jsonb")
      pgObject.setValue(map.asJson.noSpaces)
      pgObject
    })
