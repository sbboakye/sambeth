package com.sbboakye.engine.domain

import doobie.{Get, Put}

enum StageType:
  case Source, Transformation, Sink

object StageType:
  given Get[StageType] = Get[String].map(StageType.valueOf)
  given Put[StageType] = Put[String].contramap(_.toString)
