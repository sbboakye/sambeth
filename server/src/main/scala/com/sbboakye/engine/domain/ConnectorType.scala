package com.sbboakye.engine.domain

import doobie.{Get, Put}

enum ConnectorType:
  case Database, CloudStorage, API, FileSystem

object ConnectorType:
  given Get[ConnectorType] = Get[String].map(ConnectorType.valueOf)
  given Put[ConnectorType] = Put[String].contramap(_.toString)
