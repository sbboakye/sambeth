package com.sbboakye.engine.repositories.execution

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.domain.CustomTypes.ExecutionId
import com.sbboakye.engine.domain.{PipelineExecution, PipelineExecutionLog}
import com.sbboakye.engine.repositories.executionLog.PipelineExecutionLogsRepository

class PipelineExecutionLogsHelper[F[_]: MonadCancelThrow](using
                                                          executionLogsRepository: PipelineExecutionLogsRepository[F]
):

  private def loadExecutionLogs(
      executionIds: List[ExecutionId]
  ): F[Map[ExecutionId, Seq[PipelineExecutionLog]]] =
    for {
      executions <- executionLogsRepository.findAllByExecutionIds(executionIds)
    } yield executions.groupBy(_.executionId)

  def enrichExecutionsWithLogs(
      executions: Seq[PipelineExecution]
  ): F[Seq[PipelineExecution]] =
    for {
      executionIds         <- executions.map(_.id).toList.pure[F]
      groupedExecutionLogs <- loadExecutionLogs(executionIds)
    } yield executions.map(execution =>
      execution.copy(logs =
        groupedExecutionLogs.getOrElse(execution.id, Seq.empty[PipelineExecutionLog])
      )
    )
