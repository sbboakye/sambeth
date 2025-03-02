package com.sbboakye.engine.domain

import doobie.{Get, Put}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

enum StageType:
  case Source, Transformation, Sink

object StageType:
  given Get[StageType] = Get[String].map(StageType.valueOf)
  given Put[StageType] = Put[String].contramap(_.toString)

  given Encoder[StageType] = deriveEncoder[StageType]
  given Decoder[StageType] = deriveDecoder[StageType]
