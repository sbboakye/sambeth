package com.sbboakye.engine.services

import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.repositories.Repository
import doobie.Transactor

import java.util.UUID

trait CoreService[F[_], Repo[F[_]] <: Repository[F, A], A](xa: Transactor[F])(using
    setup: RepositorySetup[Repo, F]
):

  def findAll: F[Seq[A]] = setup.use(xa) { (repo, xa) =>
    repo.findAll(0, 10)
  }

  def findById(id: UUID): F[Option[A]] =
    setup.use(xa) { (repo, xa) => repo.findById(id) }

  def create(entity: A): F[UUID] = setup.use(xa) { (repo, xa) =>
    repo.create(entity)
  }

  def update(id: UUID, entity: A): F[Option[Int]] = setup.use(xa) { (repo, xa) =>
    repo.update(id, entity)
  }

  def delete(id: UUID): F[Option[Int]] = setup.use(xa) { (repo, xa) =>
    repo.delete(id)
  }
