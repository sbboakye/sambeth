package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositoryContext.NoHelper
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import doobie.Transactor

import java.util.UUID

class ConnectorService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[ConnectorsRepository, NoHelper, F]
) extends CoreService[F, ConnectorsRepository, NoHelper, Connector](xa):

  override def findAll: F[Seq[Connector]] = withDependencies { (repo, _, _) =>
    repo.findAll(0, 10)
  }

  override def findById(id: UUID): F[Option[Connector]] = withDependencies { (repo, _, _) =>
    repo.findById(id)
  }

  override def create(entity: Connector): F[UUID] = withDependencies { (repo, _, _) =>
    repo.create(entity)
  }

  override def update(id: UUID, entity: Connector): F[Option[Int]] = withDependencies {
    (repo, _, _) =>
      repo.update(id, entity)
  }

  override def delete(id: UUID): F[Option[Int]] = withDependencies { (repo, _, _) =>
    repo.delete(id)
  }

object ConnectorService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[ConnectorsRepository, NoHelper, F]
  ): Resource[F, ConnectorService[F]] =
    Resource.eval(F.pure(new ConnectorService[F](xa)))
