package com.sbboakye.engine.contexts

import doobie.Transactor

trait RepositorySetup[Repo[_[_]], F[_]]:
  def use[T](xa: Transactor[F])(run: (Repo[F], Transactor[F]) => F[T]): F[T]
