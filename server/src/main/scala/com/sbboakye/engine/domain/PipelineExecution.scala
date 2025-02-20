package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.{ExecutionId, PipelineId}
import com.sbboakye.engine.repositories.pipeline.{PipelinesRepository, StagesHelper}
import doobie.Read
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.time.OffsetDateTime

case class PipelineExecution(
    id: ExecutionId,
    pipelineId: PipelineId,
    startTime: OffsetDateTime,
    endTime: Option[OffsetDateTime],
    status: ExecutionStatus,
    logs: Seq[PipelineExecutionLog],
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def getPipeline[F[_]](using
      repository: PipelinesRepository[F]
  )(using StagesHelper[F]): F[Option[Pipeline]] =
    repository.findById(pipelineId)
}

object PipelineExecution:
  given Read[PipelineExecution] = Read[
    (
        ExecutionId,
        PipelineId,
        OffsetDateTime,
        Option[OffsetDateTime],
        ExecutionStatus,
        OffsetDateTime,
        OffsetDateTime
    )
  ]
    .map {
      case (
            id,
            pipelineId,
            startTime,
            endTime,
            status,
            createdAt,
            updatedAt
          ) =>
        PipelineExecution(
          id,
          pipelineId,
          startTime,
          endTime,
          status,
          Seq.empty[PipelineExecutionLog],
          createdAt,
          updatedAt
        )
    }
