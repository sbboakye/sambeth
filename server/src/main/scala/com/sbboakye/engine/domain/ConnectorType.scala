package com.sbboakye.engine.domain

import doobie.{Get, Put}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

enum ConnectorType:
  case Database, CloudStorage, API, FileSystem

object ConnectorType:
  given Get[ConnectorType] = Get[String].map(ConnectorType.valueOf)
  given Put[ConnectorType] = Put[String].contramap(_.toString)

  given Encoder[ConnectorType] = deriveEncoder[ConnectorType]
  given Decoder[ConnectorType] = deriveDecoder[ConnectorType]
