package com.sbboakye.engine.repositories.core

import cats.*
import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

trait Core[F[_]: MonadCancelThrow: Logger, Entity: Read]:

  private def runQuery[A](query: ConnectionIO[A])(using xa: Transactor[F]): F[A] =
    query.transact(xa)

  private def runUpdate(query: Fragment)(using xa: Transactor[F]): F[Option[Int]] =
    runQuery(query.update.run.map {
      case 0 => None
      case n => Some(n)
    })

  def findAll(select: Fragment, offset: Int, limit: Int)(using xa: Transactor[F]): F[Seq[Entity]] =
    Logger[F].info(s"Fetching all entities with offset: $offset, limit: $limit") *>
      runQuery(
        (select ++ fr"LIMIT $limit OFFSET $offset")
          .query[Entity]
          .to[Seq]
      )

  def findByID(select: Fragment, where: Fragment)(using xa: Transactor[F]): F[Option[Entity]] =
    Logger[F].info(s"Fetching an entity") *>
      runQuery(
        (select ++ where)
          .query[Entity]
          .option
      )

  def create(insertQuery: Fragment)(using xa: Transactor[F]): F[UUID] =
    Logger[F].info(s"Creating an entity") *>
      runQuery(
        insertQuery.update
          .withUniqueGeneratedKeys[UUID]("id")
      )

  def update(updateQuery: Fragment)(using
      xa: Transactor[F]
  ): F[Option[Int]] =
    Logger[F].info(s"Updating entity") *>
      runUpdate(updateQuery)

  def delete(deleteQuery: Fragment)(using xa: Transactor[F]): F[Option[Int]] =
    Logger[F].info(s"Deleting an entity") *>
      runUpdate(deleteQuery)
