package com.sbboakye.engine.repositories

import cats.effect.MonadCancelThrow

import java.util.UUID

trait Repository[F[_]: MonadCancelThrow, A] {
  def findAll(offset: Int, limit: Int): F[Seq[A]]
  def findById(id: UUID): F[Option[A]]
  def create(entity: A): F[UUID]
  def update(id: UUID, entity: A): F[Option[Int]] = MonadCancelThrow[F].pure(None)
  def delete(id: UUID): F[Option[Int]]
}
