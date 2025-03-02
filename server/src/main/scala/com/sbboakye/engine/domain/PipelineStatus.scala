package com.sbboakye.engine.domain

import doobie.{Get, Put}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

enum PipelineStatus:
  case Active, Inactive, Draft

object PipelineStatus:
  given Get[PipelineStatus] = Get[String].map(PipelineStatus.valueOf)
  given Put[PipelineStatus] = Put[String].contramap(_.toString)

  given Encoder[PipelineStatus] = deriveEncoder[PipelineStatus]
  given Decoder[PipelineStatus] = deriveDecoder[PipelineStatus]
