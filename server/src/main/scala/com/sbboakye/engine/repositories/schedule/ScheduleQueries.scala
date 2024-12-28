package com.sbboakye.engine.repositories.schedule

import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

object ScheduleQueries extends HasCommonAttributes:
  override val tableName: String = "schedules"

  val select: Fragment =
    fr"SELECT id, cron_expression, timezone, created_at, updated_at FROM" ++ Fragment.const(
      tableName
    )

  def insert(schedule: Schedule): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (cron_expression, timezone)
           VALUES (${schedule.cronExpression}, ${schedule.timezone})"""

  def update(id: UUID, schedule: Schedule): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET cron_expression = ${schedule.cronExpression},
               timezone = ${schedule.timezone}""" ++ where(id = id)
