package com.sbboakye.engine.repositories.schedule

import cats.*
import cats.effect.*
import cats.implicits.*
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.core.Core
import com.sbboakye.engine.repositories.schedule.ScheduleQueries.{select, where}
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class SchedulesRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, Schedule]
):

  def findAll(offset: Int, limit: Int): F[Seq[Schedule]] =
    core.findAll(ScheduleQueries.select, offset, limit)
  def findById(id: UUID): F[Option[Schedule]] =
    core.findByID(ScheduleQueries.select, ScheduleQueries.where(id))
  def create(schedule: Schedule): F[UUID] = core.create(ScheduleQueries.insert(schedule))
  def update(id: UUID, schedule: Schedule): F[Option[Int]] =
    core.update(ScheduleQueries.update(id, schedule))
  def delete(id: UUID): F[Option[Int]] = core.delete(ScheduleQueries.where(id))

object SchedulesRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, Schedule]
  ): Resource[F, SchedulesRepository[F]] =
    Resource.eval(Async[F].pure(new SchedulesRepository[F]))
