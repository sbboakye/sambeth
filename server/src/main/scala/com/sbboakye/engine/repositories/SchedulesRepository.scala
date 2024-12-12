package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
import cats.implicits.*
import com.sbboakye.engine.core.Core
import com.sbboakye.engine.domain.Schedule
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.time.OffsetDateTime
import java.util.UUID

class SchedulesRepository[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends Core[F, Schedule]:

  private val select: Fragment =
    fr"SELECT id, cron_expression, timezone, created_at, updated_at FROM schedules"

  private inline def where(id: UUID): Fragment = fr"WHERE id = $id"

  private def runQuery[A](query: ConnectionIO[A]): F[A] =
    query.transact(xa)

  override def findAll(offset: Int, limit: Int): F[Seq[Schedule]] =
    Logger[F].info(s"Fetching all schedules with offset: $offset, limit: $limit") *>
      runQuery((select ++ fr"LIMIT $limit OFFSET $offset").query[Schedule].to[Seq])

  override def findByID(id: UUID): F[Option[Schedule]] =
    Logger[F].info(s"Fetching schedule with id: $id") *>
      runQuery((select ++ where(id)).query[Schedule].option)

  override def create(schedule: Schedule): F[UUID] =
    Logger[F].info(s"Creating a schedule with: $schedule") *>
      runQuery(
        sql"""INSERT INTO schedules (cron_expression, timezone)
         VALUES (${schedule.cronExpression}, ${schedule.timezone})
       """.update
          .withUniqueGeneratedKeys[UUID]("id")
      )

  override def update(id: UUID, schedule: Schedule): F[Option[Int]] =
    Logger[F].info(s"Updating schedule $id with: $schedule") *>
      runQuery(
        (sql"""
         UPDATE schedules
         SET cron_expression = ${schedule.cronExpression},
             timezone = ${schedule.timezone}
       """ ++ where(id)).update.run
          .map {
            case 0 => None
            case n => Some(n)
          }
      )

  override def delete(id: UUID): F[Option[Int]] =
    Logger[F].info(s"Deleting schedule $id") *>
      runQuery(
        (sql"DELETE FROM schedules" ++ where(id)).update.run
          .map {
            case 0 => None
            case n => Some(n)
          }
      )

object SchedulesRepository:
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F])(using
      F: Async[F]
  ): Resource[F, Core[F, Schedule]] =
    Resource.eval(F.pure(new SchedulesRepository[F](xa)))
