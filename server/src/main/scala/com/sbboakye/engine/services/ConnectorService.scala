package com.sbboakye.engine.services

import cats.effect.{Concurrent, Resource}
import com.sbboakye.engine.contexts.RepositorySetup
import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.repositories.connector.ConnectorsRepository
import doobie.Transactor

import java.util.UUID

class ConnectorService[F[_]: Concurrent] private (xa: Transactor[F])(using
    RepositorySetup[ConnectorsRepository, F]
) extends CoreService[F, ConnectorsRepository, Connector](xa)

object ConnectorService:
  def apply[F[_]](xa: Transactor[F])(using
      F: Concurrent[F],
      setup: RepositorySetup[ConnectorsRepository, F]
  ): Resource[F, ConnectorService[F]] =
    Resource.eval(F.pure(new ConnectorService[F](xa)))
