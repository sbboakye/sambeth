package com.sbboakye.engine.domain

import doobie.{Get, Put}

enum PipelineStatus:
  case Active, Inactive, Draft

object PipelineStatus:
  given Get[PipelineStatus] = Get[String].map(PipelineStatus.valueOf)
  given Put[PipelineStatus] = Put[String].contramap(_.toString)
