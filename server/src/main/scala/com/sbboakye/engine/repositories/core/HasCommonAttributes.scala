package com.sbboakye.engine.repositories.core

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

trait HasCommonAttributes:
  val tableName: String

  def where(column: String = "id", id: UUID): Fragment =
    fr"WHERE ${column} = $id"

  def delete(id: UUID): Fragment =
    fr"DELETE FROM ${Fragment.const(tableName)}" ++ where(id = id)
