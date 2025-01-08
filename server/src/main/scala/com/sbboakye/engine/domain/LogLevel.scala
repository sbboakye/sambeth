package com.sbboakye.engine.domain

import doobie.{Get, Put}

enum LogLevel:
  case Info, Warn, Error

object LogLevel:
  given Get[LogLevel] = Get[String].map(LogLevel.valueOf)
  given Put[LogLevel] = Put[String].contramap(_.toString)
