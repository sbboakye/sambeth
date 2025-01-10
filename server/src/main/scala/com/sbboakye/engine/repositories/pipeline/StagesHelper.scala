package com.sbboakye.engine.repositories.pipeline

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.domain.CustomTypes.PipelineId
import com.sbboakye.engine.domain.{Pipeline, Stage}
import com.sbboakye.engine.repositories.stage.StagesRepository

class StagesHelper[F[_]: MonadCancelThrow](using
    stagesRepository: StagesRepository[F]
):

  private def loadStages(pipelineIds: List[PipelineId]): F[Map[PipelineId, Seq[Stage]]] =
    for {
      stages <- stagesRepository.findAllByPipelineIds(pipelineIds)
    } yield stages.groupBy(_.pipelineId)

  def enrichPipelinesWithStages(
      pipelines: Seq[Pipeline]
  ): F[Seq[Pipeline]] =
    for {
      pipelineIds   <- pipelines.map(_.id).toList.pure[F]
      groupedStages <- loadStages(pipelineIds)
    } yield pipelines.map(pipeline =>
      pipeline.copy(stages = groupedStages.getOrElse(pipeline.id, Seq.empty[Stage]))
    )
