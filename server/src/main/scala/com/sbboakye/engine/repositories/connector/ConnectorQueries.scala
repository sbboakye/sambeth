package com.sbboakye.engine.repositories.connector

import com.sbboakye.engine.domain.Connector
import com.sbboakye.engine.repositories.core.HasCommonAttributes
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

object ConnectorQueries extends HasCommonAttributes:
  override val tableName: String = "connectors"

  val select: Fragment =
    fr"SELECT id, stage_id, name, connector_type, configuration, created_at, updated_at FROM" ++ Fragment
      .const(
        tableName
      )

  def insert(connector: Connector): Fragment =
    fr"""INSERT INTO ${Fragment.const(tableName)} (stage_id, name, connector_type, configuration)
           VALUES (${connector.stageId}, ${connector.name}), ${connector.connectorType.toString}, ${connector.configuration}))"""

  def update(id: UUID, connector: Connector): Fragment =
    fr"""UPDATE ${Fragment.const(tableName)}
           SET stage_id = ${connector.stageId},
               name = ${connector.name},
               connector_type = ${connector.connectorType.toString},
               configuration = ${connector.configuration}
               """ ++ where(id = id)
