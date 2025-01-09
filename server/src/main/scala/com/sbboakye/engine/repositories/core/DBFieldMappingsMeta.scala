package com.sbboakye.engine.repositories.core

object DBFieldMappingsMeta:

  import doobie.Meta
  import io.circe.parser.*
  import io.circe.syntax.*
  import org.postgresql.util.PGobject

  given Meta[Map[String, String]] = Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[Map[String, String]](obj => {
      println(s"PGobject value: ${obj.getValue}")
      decode[Map[String, String]](obj.getValue).getOrElse(Map.empty)
    })(map => {
      val pgObject = new PGobject()
      pgObject.setType("jsonb")
      pgObject.setValue(map.asJson.noSpaces)
      pgObject
    })
