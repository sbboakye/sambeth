package com.sbboakye.engine.repositories.execution

import com.sbboakye.engine.domain.PipelineExecution
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

object PipelineExecutionQueries extends HasCommonAttributes:
  override val tableName: String = "executions"

  val select: Fragment =
    fr"""
       SELECT
        id,
        pipeline_id,
        start_time,
        end_time,
        status,
        created_at,
        updated_at
    FROM
        ${Fragment.const(tableName)}
    """

  def insert(execution: PipelineExecution): Fragment =
    fr"""INSERT INTO ${Fragment.const(
        tableName
      )} (pipeline_id, start_time, end_time, status)
           VALUES (${execution.pipelineId}, ${execution.startTime}, ${execution.endTime}, ${execution.status}::execution_status)"""

  def update(id: UUID, execution: PipelineExecution): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET start_time = ${execution.startTime},
               end_time = ${execution.endTime},
               status = ${execution.status}::execution_status
               """ ++ where(id = id)
