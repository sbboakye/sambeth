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
            id,
            pipeline_id,
            stage_type,
            configuration,
            position,
            created_at,
            updated_at
        FROM ${Fragment.const(tableName)}
      """

  def insert(stage: Stage): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (pipeline_id, stage_type, configuration, position)
           VALUES (${stage.pipelineId}, ${stage.stageType}::stage_type, ${stage.configuration}::jsonb, ${stage.position})"""

  def update(id: UUID, stage: Stage): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET stage_type = ${stage.stageType.toString}::stage_type,
               configuration = ${stage.configuration}::jsonb,
               position = ${stage.position}
               """ ++ where(id = id)
