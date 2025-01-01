package com.sbboakye.engine.repositories.connector

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.generic.auto.*
import doobie.{Get, Meta, Put, Read}
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import doobie.postgres.circe.json.implicits.*
import doobie.postgres.circe.jsonb.implicits.*
import org.postgresql.util.PGobject
import org.typelevel.log4cats.Logger

import java.util.UUID

class ConnectorsRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, Connector]
):

  def findAll(offset: Int, limit: Int): F[Seq[Connector]] =
    core.findAll(ConnectorQueries.select, offset, limit, ConnectorQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Connector]] =
    core.findByID(ConnectorQueries.select, ConnectorQueries.where(id = id))

  def create(pipeline: Connector): F[UUID] = core.create(ConnectorQueries.insert(pipeline))

  def update(id: UUID, pipeline: Connector): F[Option[Int]] =
    core.update(ConnectorQueries.update(id, pipeline))

  def delete(id: UUID): F[Option[Int]] = core.delete(ConnectorQueries.delete(id))

object ConnectorsRepository:

  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, Connector]
  ): Resource[F, ConnectorsRepository[F]] =
    Resource.eval(Async[F].pure(new ConnectorsRepository[F]))
