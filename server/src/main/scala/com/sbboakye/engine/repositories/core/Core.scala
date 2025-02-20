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
import scala.reflect.ClassTag

trait Core[F[_]: { MonadCancelThrow, Logger }, Entity: { Read, ClassTag }]:

  private def entityName(using ct: ClassTag[Entity]): String =
    ct.runtimeClass.getSimpleName

  private def runQuery[A](query: ConnectionIO[A])(using xa: Transactor[F]): F[A] =
    query.transact(xa)

  private def runUpdate(query: Fragment)(using xa: Transactor[F]): F[Option[Int]] =
    runQuery(query.update.run.map {
      case 0 => None
      case n => Some(n)
    })

  def findAll(select: Fragment, offset: Int, limit: Int, limitAndOffset: (Int, Int) => Fragment)(
      using xa: Transactor[F]
  ): F[Seq[Entity]] =
    Logger[F].info(s"Fetching all $entityName with offset: $offset, limit: $limit") *>
      runQuery(
        (select ++ limitAndOffset(offset, limit))
          .query[Entity]
          .to[Seq]
      )

  def findByID(select: Fragment, where: Fragment)(using xa: Transactor[F]): F[Option[Entity]] =
    Logger[F].info(s"Fetching $entityName") *>
      runQuery(
        (select ++ where)
          .query[Entity]
          .option
      )

  def create(insertQuery: Fragment)(using xa: Transactor[F]): F[UUID] =
    Logger[F].info(s"Creating $entityName") *>
      runQuery(
        insertQuery.update
          .withUniqueGeneratedKeys[UUID]("id")
      )

  def update(updateQuery: Fragment)(using
      xa: Transactor[F]
  ): F[Option[Int]] =
    Logger[F].info(s"Updating $entityName") *>
      runUpdate(updateQuery)

  def delete(deleteQuery: Fragment)(using xa: Transactor[F]): F[Option[Int]] =
    Logger[F].info(s"Deleting $entityName") *>
      runUpdate(deleteQuery)
