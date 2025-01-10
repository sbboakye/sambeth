package com.sbboakye.engine.repositories.core

import cats.*
import cats.implicits.*
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

  def whereIn(column: String, listOfIds: List[UUID]): Fragment =
    listOfIds.toNel match {
      case Some(nel) => fr"WHERE" ++ Fragments.in(Fragment.const(column), nel)
      case None      => fr"WHERE" ++ fr"1 = 0"
    }

  def delete(id: UUID): Fragment =
    fr"DELETE FROM ${Fragment.const(tableName)}" ++ where(id = id)
