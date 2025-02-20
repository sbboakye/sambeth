package com.sbboakye.engine.repositories.schedule

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.Repository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class SchedulesRepository[F[_]: { MonadCancelThrow, Logger }] private (using
    xa: Transactor[F]
) extends Core[F, Schedule]
    with Repository[F, Schedule]:

  def findAll(offset: Int, limit: Int): F[Seq[Schedule]] =
    findAll(ScheduleQueries.select, offset, limit, ScheduleQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Schedule]] =
    findByID(ScheduleQueries.select, ScheduleQueries.where(id = id))

  def create(schedule: Schedule): F[UUID] = create(ScheduleQueries.insert(schedule))

  override def update(id: UUID, schedule: Schedule): F[Option[Int]] =
    update(ScheduleQueries.update(id, schedule))

  def delete(id: UUID): F[Option[Int]] = delete(ScheduleQueries.delete(id))

object SchedulesRepository:
  def apply[F[_]: { MonadCancelThrow, Logger }](using
      Transactor[F]
  ): Resource[F, SchedulesRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new SchedulesRepository[F]))
