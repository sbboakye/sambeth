package com.sbboakye.engine.repositories.executionLog

import com.sbboakye.engine.domain.ExecutionLog
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

object ExecutionLogQueries extends HasCommonAttributes:
  override val tableName: String = "execution_logs"

  val select: Fragment =
    fr"""
       SELECT
          id,
          execution_id,
          stage_id,
          timestamp,
          message,
          log_level,
          created_at
       FROM
          ${Fragment.const(tableName)}
    """

  def insert(log: ExecutionLog): Fragment =
    fr"""INSERT INTO ${Fragment.const(
        tableName
      )} (execution_id, stage_id, timestamp, message, log_level, created_at)
           VALUES (${log.executionId}, ${log.stageId}, ${log.timestamp}, ${log.message}, ${log.logLevel}::log_level, ${log.createdAt})"""
