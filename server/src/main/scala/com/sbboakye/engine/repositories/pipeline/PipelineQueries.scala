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
        -- Pipelines table columns
        p.id AS pipeline_id,
        p.name AS pipeline_name,
        p.description AS pipeline_description,
        p.schedule_id AS pipeline_schedule_id,
        p.status AS pipeline_status,
        p.created_at AS pipeline_created_at,
        p.updated_at AS pipeline_updated_at,

        -- Stages table columns
        s.id AS stage_id,
        s.pipeline_id AS stage_pipeline_id,
        s.stage_type AS stage_type,
        s.configuration AS stage_configuration,
        s.position AS stage_position,
        s.created_at AS stage_created_at,
        s.updated_at AS stage_updated_at,

        -- Connectors table columns
        c.id AS connector_id,
        c.stage_id AS connector_stage_id,
        c.name AS connector_name,
        c.connector_type AS connector_type,
        c.configuration AS connector_configuration,
        c.created_at AS connector_created_at,
        c.updated_at AS connector_updated_at,

        -- Schedules table columns
        sch.id AS schedule_id,
        sch.cron_expression AS schedule_cron_expression,
        sch.timezone AS schedule_timezone,
        sch.created_at AS schedule_created_at,
        sch.updated_at AS schedule_updated_at

      FROM
          ${Fragment.const(tableName)} p
              LEFT JOIN
          stages s ON p.id = s.pipeline_id
              LEFT JOIN
          connectors c ON s.id = c.stage_id
              LEFT JOIN
          schedules sch ON p.schedule_id = sch.id
    """

  def insert(pipeline: Pipeline): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (name, description, schedule_id, status)
           VALUES (${pipeline.name}, ${pipeline.description}, ${pipeline.schedule.map(
        _.id
      )}, ${pipeline.status}::pipeline_status)"""

  def update(id: UUID, pipeline: Pipeline): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET name = ${pipeline.name},
               description = ${pipeline.description},
               schedule_id = ${pipeline.schedule.map(_.id)},
               status = ${pipeline.status}::pipeline_status
               """ ++ where(id = id)
