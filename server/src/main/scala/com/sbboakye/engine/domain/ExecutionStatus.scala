package com.sbboakye.engine.domain

import doobie.{Get, Put}

enum ExecutionStatus:
  case Running, Completed, Failed, Cancelled

object ExecutionStatus:
  given Get[ExecutionStatus] = Get[String].map(ExecutionStatus.valueOf)
  given Put[ExecutionStatus] = Put[String].contramap(_.toString)
