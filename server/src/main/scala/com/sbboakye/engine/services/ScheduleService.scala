package com.sbboakye.engine.services

import cats.effect.Concurrent
import cats.effect.Resource
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import doobie.Transactor

class ScheduleService[F[_]: Concurrent] private (
    xa: Transactor[F]
)(using RepositorySetup[SchedulesRepository, F])
    extends CoreService[F, SchedulesRepository, Schedule](xa)

object ScheduleService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[SchedulesRepository, F]
  ): Resource[F, ScheduleService[F]] =
    Resource.eval(F.pure(new ScheduleService[F](xa)))
