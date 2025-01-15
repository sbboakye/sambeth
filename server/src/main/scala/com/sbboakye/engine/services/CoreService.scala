package com.sbboakye.engine.services

import com.sbboakye.engine.contexts.RepositorySetup
import doobie.Transactor

import java.util.UUID

trait CoreService[F[_], Repo[_[_]], H[_[_]], A](using
    setup: RepositorySetup[Repo, H, F],
    xa: Transactor[F]
):

  def withDependencies[T](
      run: (Repo[F], H[F], Transactor[F]) => F[T]
  ): F[T] = {
    setup.use(xa) { (repo, helper, xa) =>
      run(repo, helper, xa)
    }
  }

  def findAll: F[Seq[A]]

  def findById(id: UUID): F[Option[A]]

  def create(entity: A): F[UUID]

  def update(id: UUID, entity: A): F[Option[Int]]

  def delete(id: UUID): F[Option[Int]]
