package com.sbboakye.engine.repositories

import cats.*
import cats.effect.*
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

  private def where(id: UUID): Fragment = fr"WHERE id = $id"

  override def findAll: F[Seq[Schedule]] =
    select
      .query[Schedule]
      .to[Seq]
      .transact(xa)

  override def findByID(id: UUID): F[Option[Schedule]] =
    val fullQuery = select ++ where(id)
    fullQuery
      .query[Schedule]
      .option
      .transact(xa)

  override def create(a: Schedule): F[UUID] =
    sql"""INSERT INTO schedules (cron_expression, timezone)
         VALUES (${a.cronExpression}, ${a.timezone})
       """.update
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)

  override def update(id: UUID, a: Schedule): F[Option[Int]] =
    val update         = fr"UPDATE schedules"
    val cronExpression = fr"cron_expression = ${a.cronExpression}"
    val timezone       = fr"timezone = ${a.timezone}"
    val setter         = fr"SET " ++ cronExpression ++ fr", " ++ timezone

    val fullQuery = update ++ setter ++ where(id)
    fullQuery.update.run
      .map {
        case 0 => None
        case n => Some(n)
      }
      .transact(xa)

  override def delete(id: UUID): F[Option[Int]] =
    val delete    = fr"DELETE FROM schedules"
    val fullQuery = delete ++ where(id)

    fullQuery.update.run
      .map {
        case 0 => None
        case n => Some(n)
      }
      .transact(xa)

object SchedulesRepository:
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F])(using
      F: Async[F]
  ): Resource[F, Core[F, Schedule]] =
    Resource.eval(F.pure(new SchedulesRepository[F](xa)))
