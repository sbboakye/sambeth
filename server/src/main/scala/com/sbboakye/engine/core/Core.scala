package com.sbboakye.engine.core

import java.util.UUID

trait Core[F[_], A]:
  def findAll(offset: Int, limit: Int): F[Seq[A]]
  def findByID(id: UUID): F[Option[A]]
  def create(a: A): F[UUID]
  def update(id: UUID, a: A): F[Option[Int]]
  def delete(id: UUID): F[Option[Int]]
