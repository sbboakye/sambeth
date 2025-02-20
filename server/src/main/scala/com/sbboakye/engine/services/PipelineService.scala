package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Pipeline
import com.sbboakye.engine.repositories.pipeline.{PipelinesRepository, StagesHelper}
import doobie.Transactor

import java.util.UUID

class PipelineService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[PipelinesRepository, StagesHelper, F]
) extends CoreService[F, PipelinesRepository, StagesHelper, Pipeline](xa):

  override def findAll: F[Seq[Pipeline]] = withDependencies { (repo, helper, _) =>
    repo.findAll(0, 10, helper)
  }

  override def findById(id: UUID): F[Option[Pipeline]] =
    withDependencies { (repo, helper, _) => repo.findById(id, helper) }

  override def create(entity: Pipeline): F[UUID] = withDependencies { (repo, _, _) =>
    repo.create(entity)
  }

  override def update(id: UUID, entity: Pipeline): F[Option[Int]] = withDependencies {
    (repo, _, _) =>
      repo.update(id, entity)
  }

  override def delete(id: UUID): F[Option[Int]] = withDependencies { (repo, _, _) =>
    repo.delete(id)
  }

object PipelineService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[PipelinesRepository, StagesHelper, F]
  ): Resource[F, PipelineService[F]] =
    Resource.eval(F.pure(new PipelineService[F](xa)))
