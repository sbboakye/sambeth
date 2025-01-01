package com.sbboakye.engine.repositories.core

import com.sbboakye.engine.repositories.stage.StageQueries.tableName
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

trait HasCommonAttributes:
  val tableName: String

  def limitAndOffset(offset: Int, limit: Int): Fragment =
    fr"LIMIT $limit OFFSET $offset"

  def where(column: String = "id", id: UUID): Fragment =
    fr"WHERE ${Fragment.const(column)} = $id"

  def delete(id: UUID): Fragment =
    fr"DELETE FROM ${Fragment.const(tableName)}" ++ where(id = id)
