package com.sbboakye.engine.repositories.stage

import com.sbboakye.engine.domain.Stage
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

object StageQueries extends HasCommonAttributes:
  override val tableName: String = "stages"

  val select: Fragment =
    fr"""
        SELECT
            s.id AS stage_id, s.pipeline_id AS pipeline_id, s.stage_type AS stage_type, s.configuration AS stage_configuration, s.position AS stage_position, s.created_at AS stage_created_at, s.updated_at AS stage_updated_at,
            c.id AS connector_id, c.stage_id AS connector_stage_id, c.name AS connector_name, c.connector_type AS connector_type, c.configuration AS connector_configuration, c.created_at AS connector_created_at, c.updated_at AS connector_updated_at
        FROM ${Fragment.const(tableName)} s
        LEFT JOIN connectors c ON s.id = c.stage_id
      """

  def insert(stage: Stage): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (pipeline_id, stage_type, configuration, position)
           VALUES (${stage.pipelineID}, ${stage.stageType}::stage_type, ${stage.configuration}::jsonb, ${stage.position})"""

  def update(id: UUID, stage: Stage): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET pipeline_id = ${stage.pipelineID},
               stage_type = ${stage.stageType.toString}::stage_type,
               configuration = ${stage.configuration}::jsonb,
               position = ${stage.position}
               """ ++ where(id = id)
