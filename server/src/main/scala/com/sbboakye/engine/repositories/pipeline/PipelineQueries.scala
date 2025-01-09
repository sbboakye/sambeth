package com.sbboakye.engine.repositories.pipeline

import com.sbboakye.engine.domain.Pipeline
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

object PipelineQueries extends HasCommonAttributes:
  override val tableName: String = "pipelines"

  val select: Fragment =
    fr"""
       SELECT
        id,
        name,
        description,
        schedule_id,
        status,
        created_at,
        updated_at
      FROM
          ${Fragment.const(tableName)}
    """

  def insert(pipeline: Pipeline): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (name, description, schedule_id, status)
           VALUES (${pipeline.name}, ${pipeline.description}, ${pipeline.scheduleId}, ${pipeline.status}::pipeline_status)"""

  def update(id: UUID, pipeline: Pipeline): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET name = ${pipeline.name},
               description = ${pipeline.description},
               schedule_id = ${pipeline.scheduleId},
               status = ${pipeline.status}::pipeline_status
               """ ++ where(id = id)
