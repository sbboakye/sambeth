package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Stage
import com.sbboakye.engine.repositories.stage.StagesRepository
import doobie.Transactor

import java.util.UUID

class StageService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[StagesRepository, F]
) extends CoreService[F, StagesRepository, Stage](xa)

object StageService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[StagesRepository, F]
  ): Resource[F, StageService[F]] =
    Resource.eval(F.pure(new StageService[F](xa)))
