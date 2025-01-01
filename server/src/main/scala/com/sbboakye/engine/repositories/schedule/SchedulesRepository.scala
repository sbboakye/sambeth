package com.sbboakye.engine.repositories.schedule

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class SchedulesRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, Schedule]
):

  def findAll(offset: Int, limit: Int): F[Seq[Schedule]] =
    core.findAll(ScheduleQueries.select, offset, limit, ScheduleQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Schedule]] =
    core.findByID(ScheduleQueries.select, ScheduleQueries.where(id = id))

  def create(schedule: Schedule): F[UUID] = core.create(ScheduleQueries.insert(schedule))

  def update(id: UUID, schedule: Schedule): F[Option[Int]] =
    core.update(ScheduleQueries.update(id, schedule))

  def delete(id: UUID): F[Option[Int]] = core.delete(ScheduleQueries.delete(id))

object SchedulesRepository:
  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, Schedule]
  ): Resource[F, SchedulesRepository[F]] =
    Resource.eval(Async[F].pure(new SchedulesRepository[F]))
