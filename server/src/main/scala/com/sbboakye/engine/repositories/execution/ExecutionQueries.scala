//package com.sbboakye.engine.repositories.execution
//
//import com.sbboakye.engine.domain.Execution
//import com.sbboakye.engine.repositories.core.HasCommonAttributes
//import doobie.*
//import doobie.implicits.*
//import doobie.postgres.*
//import doobie.postgres.implicits.*
//
//import java.util.UUID
//
//object ExecutionQueries extends HasCommonAttributes:
//  override val tableName: String = "executions"
//
//  val select: Fragment =
//    fr"""
//       SELECT
//        -- Executions table columns
//        e.id AS execution_id,
//        e.pipeline_id AS execution_pipeline_id,
//        e.start_time AS execution_start_time,
//        e.end_time AS execution_end_time,
//        e.status AS execution_status,
//        e.created_at AS execution_created_at,
//        e.updated_at AS execution_updated_at,
//
//        -- Execution Logs table columns
//        el.id AS execution_log_id,
//        el.stage_id AS log_stage_id,
//        el.timestamp AS log_timestamp,
//        el.message AS log_message,
//        el.log_level AS log_level,
//        el.created_at AS log_created_at,
//
//        -- Pipelines table columns
//        p.id AS pipeline_id,
//        p.name AS pipeline_name,
//        p.description AS pipeline_description,
//        p.schedule_id AS pipeline_schedule_id,
//        p.status AS pipeline_status,
//        p.created_at AS pipeline_created_at,
//        p.updated_at AS pipeline_updated_at
//    FROM
//        ${Fragment.const(tableName)} e
//    JOIN
//        execution_logs el ON e.id = el.execution_id
//    JOIN
//        pipelines p ON e.pipeline_id = p.id
//    """
//
//  def insert(execution: Execution): Fragment =
//    fr"""INSERT INTO ${Fragment.const(
//        tableName
//      )} (pipeline_id, start_time, end_time, status)
//           VALUES (${execution.pipeline.id}, ${execution.startTime}, ${execution.endTime}, ${execution.status}::execution_status)"""
//
//  def update(id: UUID, execution: Execution): Fragment =
//    fr"""UPDATE ${Fragment.const(tableName)}
//           SET start_time = ${execution.startTime},
//               end_time = ${execution.endTime},
//               status = ${execution.status}::execution_status
//               """ ++ where(id = id)
