package com.sbboakye.engine.core

import cats.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

trait Core[F[_]: MonadCancelThrow: Logger, Entity]:

  def runQuery[A](query: ConnectionIO[A])(using xa: Transactor[F]): F[A] =
    query.transact(xa)

  val select: Fragment

  def where(id: UUID): Fragment = fr"WHERE id = $id"

  def findAll(offset: Int, limit: Int): F[Seq[Entity]]
  def findByID(id: UUID): F[Option[Entity]]
  def create(a: Entity): F[UUID]
  def update(id: UUID, a: Entity): F[Option[Int]]
  def delete(id: UUID): F[Option[Int]]
