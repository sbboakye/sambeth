package com.sbboakye.engine.contexts

import doobie.Transactor

trait RepositorySetup[Repo[_[_]], H[_[_]], F[_]]:
  def use[T](xa: Transactor[F])(run: (Repo[F], H[F], Transactor[F]) => F[T]): F[T]
