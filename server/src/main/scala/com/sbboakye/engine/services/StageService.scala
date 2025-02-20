package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Stage
import com.sbboakye.engine.repositories.stage.{ConnectorsHelper, StagesRepository}
import doobie.Transactor

import java.util.UUID

class StageService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[StagesRepository, ConnectorsHelper, F]
) extends CoreService[F, StagesRepository, ConnectorsHelper, Stage](xa):

  override def findAll: F[Seq[Stage]] = withDependencies { (repo, helper, _) =>
    repo.findAll(0, 10, helper)
  }

  override def findById(id: UUID): F[Option[Stage]] = withDependencies { (repo, helper, _) =>
    repo.findById(id, helper)
  }

  override def create(entity: Stage): F[UUID] = withDependencies { (repo, _, _) =>
    repo.create(entity)
  }

  override def update(id: UUID, entity: Stage): F[Option[Int]] = withDependencies { (repo, _, _) =>
    repo.update(id, entity)
  }

  override def delete(id: UUID): F[Option[Int]] = withDependencies { (repo, _, _) =>
    repo.delete(id)
  }

object StageService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[StagesRepository, ConnectorsHelper, F]
  ): Resource[F, StageService[F]] =
    Resource.eval(F.pure(new StageService[F](xa)))
