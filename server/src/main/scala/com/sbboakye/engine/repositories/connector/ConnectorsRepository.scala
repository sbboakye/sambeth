package com.sbboakye.engine.repositories.connector

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.domain.CustomTypes.StageId
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.generic.auto.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import org.typelevel.log4cats.Logger

import java.util.UUID

class ConnectorsRepository[F[_]: MonadCancelThrow: Logger] private (using
    xa: Transactor[F],
    core: Core[F, Connector]
):

  import com.sbboakye.engine.repositories.core.DBFieldMappingsMeta.given

  def findAll(offset: Int, limit: Int): F[Seq[Connector]] =
    core.findAll(ConnectorQueries.select, offset, limit, ConnectorQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Connector]] =
    core.findByID(ConnectorQueries.select, ConnectorQueries.where(id = id))

  def create(connector: Connector): F[UUID] = core.create(ConnectorQueries.insert(connector))

  def update(id: UUID, connector: Connector): F[Option[Int]] =
    core.update(ConnectorQueries.update(id, connector))

  def delete(id: UUID): F[Option[Int]] = core.delete(ConnectorQueries.delete(id))

  def findAllByStageIds(ids: List[StageId]): F[Seq[Connector]] =
    (ConnectorQueries.select ++ ConnectorQueries.whereIn("stage_id", ids))
      .query[Connector]
      .to[Seq]
      .transact(xa)

object ConnectorsRepository:

  def apply[F[_]: Async: Logger](using
      xa: Transactor[F],
      core: Core[F, Connector]
  ): Resource[F, ConnectorsRepository[F]] =
    Resource.eval(Async[F].pure(new ConnectorsRepository[F]))
