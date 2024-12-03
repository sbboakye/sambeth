package com.sbboakye.engine.definitions
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends Tables {
  val profile: slick.jdbc.JdbcProfile = slick.jdbc.PostgresProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for
  // tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Connectors.schema, ExecutionLogs.schema, Executions.schema, Pipelines.schema, Schedules.schema, Stages.schema).reduceLeft(_ ++ _)

  /** Entity class storing rows of table Connectors
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param stageId Database column stage_id SqlType(uuid), Default(None)
   *  @param name Database column name SqlType(varchar)
   *  @param connectorType Database column connector_type SqlType(connector_type)
   *  @param configuration Database column configuration SqlType(jsonb), Length(2147483647,false)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class ConnectorsRow(id: java.util.UUID, stageId: Option[java.util.UUID] = None, name: String, connectorType: String, configuration: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching ConnectorsRow objects using plain SQL queries */
  implicit def GetResultConnectorsRow(implicit e0: GR[java.util.UUID], e1: GR[Option[java.util.UUID]], e2: GR[String], e3: GR[java.sql.Timestamp]): GR[ConnectorsRow] = GR{
    prs => import prs._
    (ConnectorsRow.apply _).tupled((<<[java.util.UUID], <<?[java.util.UUID], <<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table connectors. Objects of this class serve as prototypes for rows in queries. */
  class Connectors(_tableTag: Tag) extends profile.api.Table[ConnectorsRow](_tableTag, "connectors") {
    def * = ((id, stageId, name, connectorType, configuration, createdAt, updatedAt)).mapTo[ConnectorsRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), stageId, Rep.Some(name), Rep.Some(connectorType), Rep.Some(configuration), Rep.Some(createdAt), Rep.Some(updatedAt))).shaped.<>({r=>import r._; _1.map(_=> (ConnectorsRow.apply _).tupled((_1.get, _2, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column stage_id SqlType(uuid), Default(None) */
    val stageId: Rep[Option[java.util.UUID]] = column[Option[java.util.UUID]]("stage_id", O.Default(None))
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column connector_type SqlType(connector_type) */
    val connectorType: Rep[String] = column[String]("connector_type")
    /** Database column configuration SqlType(jsonb), Length(2147483647,false) */
    val configuration: Rep[String] = column[String]("configuration", O.Length(2147483647,varying=false))
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Foreign key referencing Stages (database name connectors_stage_id_fkey) */
    lazy val stagesFk = foreignKey("connectors_stage_id_fkey", stageId, Stages)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Connectors */
  lazy val Connectors = new TableQuery(tag => new Connectors(tag))

  /** Entity class storing rows of table ExecutionLogs
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param executionId Database column execution_id SqlType(uuid)
   *  @param stageId Database column stage_id SqlType(uuid), Default(None)
   *  @param timestamp Database column timestamp SqlType(timestamptz)
   *  @param message Database column message SqlType(text)
   *  @param logLevel Database column log_level SqlType(log_level)
   *  @param createdAt Database column created_at SqlType(timestamptz) */
  case class ExecutionLogsRow(id: java.util.UUID, executionId: java.util.UUID, stageId: Option[java.util.UUID] = None, timestamp: java.sql.Timestamp, message: String, logLevel: String, createdAt: java.sql.Timestamp)
  /** GetResult implicit for fetching ExecutionLogsRow objects using plain SQL queries */
  implicit def GetResultExecutionLogsRow(implicit e0: GR[java.util.UUID], e1: GR[Option[java.util.UUID]], e2: GR[java.sql.Timestamp], e3: GR[String]): GR[ExecutionLogsRow] = GR{
    prs => import prs._
    (ExecutionLogsRow.apply _).tupled((<<[java.util.UUID], <<[java.util.UUID], <<?[java.util.UUID], <<[java.sql.Timestamp], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table execution_logs. Objects of this class serve as prototypes for rows in queries. */
  class ExecutionLogs(_tableTag: Tag) extends profile.api.Table[ExecutionLogsRow](_tableTag, "execution_logs") {
    def * = ((id, executionId, stageId, timestamp, message, logLevel, createdAt)).mapTo[ExecutionLogsRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(executionId), stageId, Rep.Some(timestamp), Rep.Some(message), Rep.Some(logLevel), Rep.Some(createdAt))).shaped.<>({r=>import r._; _1.map(_=> (ExecutionLogsRow.apply _).tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column execution_id SqlType(uuid) */
    val executionId: Rep[java.util.UUID] = column[java.util.UUID]("execution_id")
    /** Database column stage_id SqlType(uuid), Default(None) */
    val stageId: Rep[Option[java.util.UUID]] = column[Option[java.util.UUID]]("stage_id", O.Default(None))
    /** Database column timestamp SqlType(timestamptz) */
    val timestamp: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("timestamp")
    /** Database column message SqlType(text) */
    val message: Rep[String] = column[String]("message")
    /** Database column log_level SqlType(log_level) */
    val logLevel: Rep[String] = column[String]("log_level")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    /** Foreign key referencing Executions (database name execution_logs_execution_id_fkey) */
    lazy val executionsFk = foreignKey("execution_logs_execution_id_fkey", executionId, Executions)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Stages (database name execution_logs_stage_id_fkey) */
    lazy val stagesFk = foreignKey("execution_logs_stage_id_fkey", stageId, Stages)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ExecutionLogs */
  lazy val ExecutionLogs = new TableQuery(tag => new ExecutionLogs(tag))

  /** Entity class storing rows of table Executions
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param pipelineId Database column pipeline_id SqlType(uuid)
   *  @param startTime Database column start_time SqlType(timestamptz)
   *  @param endTime Database column end_time SqlType(timestamptz), Default(None)
   *  @param status Database column status SqlType(execution_status)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class ExecutionsRow(id: java.util.UUID, pipelineId: java.util.UUID, startTime: java.sql.Timestamp, endTime: Option[java.sql.Timestamp] = None, status: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching ExecutionsRow objects using plain SQL queries */
  implicit def GetResultExecutionsRow(implicit e0: GR[java.util.UUID], e1: GR[java.sql.Timestamp], e2: GR[Option[java.sql.Timestamp]], e3: GR[String]): GR[ExecutionsRow] = GR{
    prs => import prs._
    (ExecutionsRow.apply _).tupled((<<[java.util.UUID], <<[java.util.UUID], <<[java.sql.Timestamp], <<?[java.sql.Timestamp], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table executions. Objects of this class serve as prototypes for rows in queries. */
  class Executions(_tableTag: Tag) extends profile.api.Table[ExecutionsRow](_tableTag, "executions") {
    def * = ((id, pipelineId, startTime, endTime, status, createdAt, updatedAt)).mapTo[ExecutionsRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(pipelineId), Rep.Some(startTime), endTime, Rep.Some(status), Rep.Some(createdAt), Rep.Some(updatedAt))).shaped.<>({r=>import r._; _1.map(_=> (ExecutionsRow.apply _).tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column pipeline_id SqlType(uuid) */
    val pipelineId: Rep[java.util.UUID] = column[java.util.UUID]("pipeline_id")
    /** Database column start_time SqlType(timestamptz) */
    val startTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_time")
    /** Database column end_time SqlType(timestamptz), Default(None) */
    val endTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("end_time", O.Default(None))
    /** Database column status SqlType(execution_status) */
    val status: Rep[String] = column[String]("status")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Foreign key referencing Pipelines (database name executions_pipeline_id_fkey) */
    lazy val pipelinesFk = foreignKey("executions_pipeline_id_fkey", pipelineId, Pipelines)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Executions */
  lazy val Executions = new TableQuery(tag => new Executions(tag))

  /** Entity class storing rows of table Pipelines
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param description Database column description SqlType(text), Default(None)
   *  @param scheduleId Database column schedule_id SqlType(uuid), Default(None)
   *  @param status Database column status SqlType(pipeline_status)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class PipelinesRow(id: java.util.UUID, name: String, description: Option[String] = None, scheduleId: Option[java.util.UUID] = None, status: Option[String], createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching PipelinesRow objects using plain SQL queries */
  implicit def GetResultPipelinesRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[java.util.UUID]], e4: GR[java.sql.Timestamp]): GR[PipelinesRow] = GR{
    prs => import prs._
    (PipelinesRow.apply _).tupled((<<[java.util.UUID], <<[String], <<?[String], <<?[java.util.UUID], <<?[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table pipelines. Objects of this class serve as prototypes for rows in queries. */
  class Pipelines(_tableTag: Tag) extends profile.api.Table[PipelinesRow](_tableTag, "pipelines") {
    def * = ((id, name, description, scheduleId, status, createdAt, updatedAt)).mapTo[PipelinesRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), description, scheduleId, status, Rep.Some(createdAt), Rep.Some(updatedAt))).shaped.<>({r=>import r._; _1.map(_=> (PipelinesRow.apply _).tupled((_1.get, _2.get, _3, _4, _5, _6.get, _7.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column schedule_id SqlType(uuid), Default(None) */
    val scheduleId: Rep[Option[java.util.UUID]] = column[Option[java.util.UUID]]("schedule_id", O.Default(None))
    /** Database column status SqlType(pipeline_status) */
    val status: Rep[Option[String]] = column[Option[String]]("status")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Foreign key referencing Schedules (database name pipelines_schedule_id_fkey) */
    lazy val schedulesFk = foreignKey("pipelines_schedule_id_fkey", scheduleId, Schedules)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Pipelines */
  lazy val Pipelines = new TableQuery(tag => new Pipelines(tag))

  /** Entity class storing rows of table Schedules
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param cronExpression Database column cron_expression SqlType(varchar)
   *  @param timezone Database column timezone SqlType(varchar)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class SchedulesRow(id: java.util.UUID, cronExpression: String, timezone: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching SchedulesRow objects using plain SQL queries */
  implicit def GetResultSchedulesRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[SchedulesRow] = GR{
    prs => import prs._
    (SchedulesRow.apply _).tupled((<<[java.util.UUID], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table schedules. Objects of this class serve as prototypes for rows in queries. */
  class Schedules(_tableTag: Tag) extends profile.api.Table[SchedulesRow](_tableTag, "schedules") {
    def * = ((id, cronExpression, timezone, createdAt, updatedAt)).mapTo[SchedulesRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(cronExpression), Rep.Some(timezone), Rep.Some(createdAt), Rep.Some(updatedAt))).shaped.<>({r=>import r._; _1.map(_=> (SchedulesRow.apply _).tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column cron_expression SqlType(varchar) */
    val cronExpression: Rep[String] = column[String]("cron_expression")
    /** Database column timezone SqlType(varchar) */
    val timezone: Rep[String] = column[String]("timezone")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")
  }
  /** Collection-like TableQuery object for table Schedules */
  lazy val Schedules = new TableQuery(tag => new Schedules(tag))

  /** Entity class storing rows of table Stages
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param pipelineId Database column pipeline_id SqlType(uuid)
   *  @param stageType Database column stage_type SqlType(stage_type)
   *  @param configuration Database column configuration SqlType(jsonb), Length(2147483647,false)
   *  @param position Database column position SqlType(int4)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class StagesRow(id: java.util.UUID, pipelineId: java.util.UUID, stageType: String, configuration: String, position: Int, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching StagesRow objects using plain SQL queries */
  implicit def GetResultStagesRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Timestamp]): GR[StagesRow] = GR{
    prs => import prs._
    (StagesRow.apply _).tupled((<<[java.util.UUID], <<[java.util.UUID], <<[String], <<[String], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table stages. Objects of this class serve as prototypes for rows in queries. */
  class Stages(_tableTag: Tag) extends profile.api.Table[StagesRow](_tableTag, "stages") {
    def * = ((id, pipelineId, stageType, configuration, position, createdAt, updatedAt)).mapTo[StagesRow]
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(pipelineId), Rep.Some(stageType), Rep.Some(configuration), Rep.Some(position), Rep.Some(createdAt), Rep.Some(updatedAt))).shaped.<>({r=>import r._; _1.map(_=> (StagesRow.apply _).tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column pipeline_id SqlType(uuid) */
    val pipelineId: Rep[java.util.UUID] = column[java.util.UUID]("pipeline_id")
    /** Database column stage_type SqlType(stage_type) */
    val stageType: Rep[String] = column[String]("stage_type")
    /** Database column configuration SqlType(jsonb), Length(2147483647,false) */
    val configuration: Rep[String] = column[String]("configuration", O.Length(2147483647,varying=false))
    /** Database column position SqlType(int4) */
    val position: Rep[Int] = column[Int]("position")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    /** Foreign key referencing Pipelines (database name stages_pipeline_id_fkey) */
    lazy val pipelinesFk = foreignKey("stages_pipeline_id_fkey", pipelineId, Pipelines)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Stages */
  lazy val Stages = new TableQuery(tag => new Stages(tag))
}
