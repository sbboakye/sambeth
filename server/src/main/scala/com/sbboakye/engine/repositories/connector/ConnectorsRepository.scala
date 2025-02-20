package com.sbboakye.engine.repositories.connector

import cats.*
import cats.effect.*
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.domain.CustomTypes.StageId
import com.sbboakye.engine.repositories.Repository
import com.sbboakye.engine.repositories.core.Core
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class ConnectorsRepository[F[_]: { MonadCancelThrow, Logger }](using
    xa: Transactor[F]
) extends Core[F, Connector]
    with Repository[F, Connector]:

  def findAll(offset: Int, limit: Int): F[Seq[Connector]] =
    findAll(ConnectorQueries.select, offset, limit, ConnectorQueries.limitAndOffset)

  def findById(id: UUID): F[Option[Connector]] =
    findByID(ConnectorQueries.select, ConnectorQueries.where(id = id))

  def create(connector: Connector): F[UUID] = create(ConnectorQueries.insert(connector))

  override def update(id: UUID, connector: Connector): F[Option[Int]] =
    update(ConnectorQueries.update(id, connector))

  def delete(id: UUID): F[Option[Int]] = delete(ConnectorQueries.delete(id))

  def findAllByStageIds(ids: List[StageId]): F[Seq[Connector]] =
    (ConnectorQueries.select ++ ConnectorQueries.whereIn("stage_id", ids))
      .query[Connector]
      .to[Seq]
      .transact(xa)

object ConnectorsRepository:

  def apply[F[_]: { MonadCancelThrow, Logger }](using
      Transactor[F]
  ): Resource[F, ConnectorsRepository[F]] =
    Resource.eval(MonadCancelThrow[F].pure(new ConnectorsRepository[F]))
