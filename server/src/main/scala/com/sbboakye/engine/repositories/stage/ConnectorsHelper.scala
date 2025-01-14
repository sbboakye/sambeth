package com.sbboakye.engine.repositories.stage

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.domain.{Connector, Stage}
import com.sbboakye.engine.domain.CustomTypes.StageId
import com.sbboakye.engine.repositories.connector.ConnectorsRepository

class ConnectorsHelper[F[_]: MonadCancelThrow](using connectorsRepository: ConnectorsRepository[F]):

  private def loadConnectors(listOfIds: List[StageId]): F[Map[StageId, Seq[Connector]]] =
    for {
      connectors <- connectorsRepository.findAllByStageIds(listOfIds)
    } yield connectors.groupBy(_.stageId)

  def enrichStagesWithConnectors(
      stages: Seq[Stage]
  ): F[Seq[Stage]] =
    for {
      stageIds          <- stages.map(_.id).toList.pure[F]
      groupedConnectors <- loadConnectors(stageIds)
    } yield stages.map(stage =>
      stage.copy(connectors = groupedConnectors.getOrElse(stage.id, Seq.empty[Connector]))
    )
