CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TYPE PIPELINE_STATUS AS ENUM ('Active', 'Inactive', 'Draft');
CREATE TYPE STAGE_TYPE AS ENUM ('Source', 'Transformation', 'Sink');
CREATE TYPE CONNECTOR_TYPE AS ENUM ('Database', 'CloudStorage', 'API', 'FileSystem');
CREATE TYPE EXECUTION_STATUS AS ENUM ('Running', 'Completed', 'Failed', 'Cancelled');
CREATE TYPE LOG_LEVEL AS ENUM ('Info', 'Warn', 'Error');

-- SCHEDULES
CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cron_expression VARCHAR NOT NULL CHECK (cron_expression <> ''),
    timezone VARCHAR NOT NULL CHECK (timezone <> ''),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION schedules_update_updated_at() RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER set_updated_date
    BEFORE UPDATE
    ON schedules
    FOR EACH ROW
EXECUTE FUNCTION schedules_update_updated_at();
-- END SCHEDULES

-- PIPELINES
CREATE TABLE pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR NOT NULL,
    description TEXT,
    schedule_id UUID REFERENCES schedules(id),
    status PIPELINE_STATUS DEFAULT 'Draft',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION pipelines_update_updated_at() RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER set_updated_date
    BEFORE UPDATE
    ON pipelines
    FOR EACH ROW
EXECUTE FUNCTION pipelines_update_updated_at();
-- END PIPELINES

-- STAGES
CREATE TABLE stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    stage_type STAGE_TYPE NOT NULL,
    configuration JSONB NOT NULL,
    position INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION stages_update_updated_at() RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER set_updated_date
    BEFORE UPDATE
    ON stages
    FOR EACH ROW
EXECUTE FUNCTION stages_update_updated_at();
-- END STAGES

-- CONNECTORS
CREATE TABLE connectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id UUID REFERENCES stages(id),
    name VARCHAR NOT NULL,
    connector_type CONNECTOR_TYPE NOT NULL,
    configuration JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION connectors_update_updated_at() RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER set_updated_date
    BEFORE UPDATE
    ON connectors
    FOR EACH ROW
EXECUTE FUNCTION connectors_update_updated_at();
-- END CONNECTORS

-- EXECUTIONS
CREATE TABLE executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL REFERENCES pipelines(id),
    start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ,
    status EXECUTION_STATUS NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION executions_update_updated_at() RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER set_updated_date
    BEFORE UPDATE
    ON executions
    FOR EACH ROW
EXECUTE FUNCTION executions_update_updated_at();
-- END EXECUTIONS

-- EXECUTIONS LOGS
CREATE TABLE execution_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    stage_id UUID REFERENCES stages(id),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message TEXT NOT NULL,
    log_level LOG_LEVEL NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- END EXECUTION LOGS

--FOR TESTING
-- INSERT INTO pipelines (id, name, description, schedule_id, status)
-- VALUES
--     ('11111111-1111-1111-1111-111111111111', 'Pipeline 1', 'Description for Pipeline 1', NULL, 'Active'),
--     ('11111111-1111-1111-1111-111111111112', 'Pipeline 2', 'Description for Pipeline 2', NULL, 'Inactive'),
--     ('11111111-1111-1111-1111-111111111113', 'Pipeline 3', 'Description for Pipeline 3', NULL, 'Draft'),
--     ('11111111-1111-1111-1111-111111111114', 'Pipeline 4', 'Description for Pipeline 4', NULL, 'Active'),
--     ('11111111-1111-1111-1111-111111111115', 'Pipeline 5', 'Description for Pipeline 5', NULL, 'Inactive'),
--     ('11111111-1111-1111-1111-111111111116', 'Pipeline 6', 'Description for Pipeline 6', NULL, 'Draft'),
--     ('11111111-1111-1111-1111-111111111117', 'Pipeline 7', 'Description for Pipeline 7', NULL, 'Active'),
--     ('11111111-1111-1111-1111-111111111118', 'Pipeline 8', 'Description for Pipeline 8', NULL, 'Inactive'),
--     ('11111111-1111-1111-1111-111111111119', 'Pipeline 9', 'Description for Pipeline 9', NULL, 'Draft'),
--     ('11111111-1111-1111-1111-111111111120', 'Pipeline 10', 'Description for Pipeline 10', NULL, 'Active');
--
-- INSERT INTO stages (id, pipeline_id, stage_type, configuration, position)
-- VALUES
--     ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111', 'Source', '{"key":"value1"}', 1),
--     ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Transformation', '{"key":"value2"}', 2),
--     ('22222222-2222-2222-2222-222222222223', '11111111-1111-1111-1111-111111111112', 'Source', '{"key":"value3"}', 1),
--     ('22222222-2222-2222-2222-222222222224', '11111111-1111-1111-1111-111111111112', 'Sink', '{"key":"value4"}', 2),
--     ('22222222-2222-2222-2222-222222222225', '11111111-1111-1111-1111-111111111113', 'Transformation', '{"key":"value5"}', 1),
--     ('22222222-2222-2222-2222-222222222226', '11111111-1111-1111-1111-111111111113', 'Source', '{"key":"value6"}', 2),
--     ('22222222-2222-2222-2222-222222222227', '11111111-1111-1111-1111-111111111114', 'Sink', '{"key":"value7"}', 1),
--     ('22222222-2222-2222-2222-222222222228', '11111111-1111-1111-1111-111111111115', 'Transformation', '{"key":"value8"}', 1),
--     ('22222222-2222-2222-2222-222222222229', '11111111-1111-1111-1111-111111111116', 'Source', '{"key":"value9"}', 1),
--     ('22222222-2222-2222-2222-222222222230', '11111111-1111-1111-1111-111111111117', 'Sink', '{"key":"value10"}', 1);
--
-- INSERT INTO connectors (id, stage_id, name, connector_type, configuration)
-- VALUES
--     ('33333333-3333-3333-3333-333333333331', '22222222-2222-2222-2222-222222222221', 'Connector 1', 'Database', '{"db":"postgres"}'),
--     ('33333333-3333-3333-3333-333333333332', '22222222-2222-2222-2222-222222222222', 'Connector 2', 'CloudStorage', '{"bucket":"s3"}'),
--     ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222223', 'Connector 3', 'API', '{"endpoint":"api.example.com"}'),
--     ('33333333-3333-3333-3333-333333333334', '22222222-2222-2222-2222-222222222224', 'Connector 4', 'FileSystem', '{"path":"/data"}'),
--     ('33333333-3333-3333-3333-333333333335', '22222222-2222-2222-2222-222222222225', 'Connector 5', 'Database', '{"db":"mysql"}'),
--     ('33333333-3333-3333-3333-333333333336', '22222222-2222-2222-2222-222222222226', 'Connector 6', 'CloudStorage', '{"bucket":"gcs"}'),
--     ('33333333-3333-3333-3333-333333333337', '22222222-2222-2222-2222-222222222227', 'Connector 7', 'API', '{"endpoint":"another-api.com"}'),
--     ('33333333-3333-3333-3333-333333333338', '22222222-2222-2222-2222-222222222228', 'Connector 8', 'FileSystem', '{"path":"/mnt"}'),
--     ('33333333-3333-3333-3333-333333333339', '22222222-2222-2222-2222-222222222229', 'Connector 9', 'Database', '{"db":"sqlite"}'),
--     ('33333333-3333-3333-3333-333333333340', '22222222-2222-2222-2222-222222222230', 'Connector 10', 'CloudStorage', '{"bucket":"azure"}');
--
-- INSERT INTO executions (id, pipeline_id, start_time, end_time, status)
-- VALUES
--     ('44444444-4444-4444-4444-444444444441', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP, NULL, 'Running'),
--     ('44444444-4444-4444-4444-444444444442', '11111111-1111-1111-1111-111111111112', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Completed'),
--     ('44444444-4444-4444-4444-444444444443', '11111111-1111-1111-1111-111111111113', CURRENT_TIMESTAMP, NULL, 'Failed'),
--     ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111114', CURRENT_TIMESTAMP, NULL, 'Running'),
--     ('44444444-4444-4444-4444-444444444445', '11111111-1111-1111-1111-111111111115', CURRENT_TIMESTAMP, NULL, 'Cancelled'),
--     ('44444444-4444-4444-4444-444444444446', '11111111-1111-1111-1111-111111111116', CURRENT_TIMESTAMP, NULL, 'Running'),
--     ('44444444-4444-4444-4444-444444444447', '11111111-1111-1111-1111-111111111117', CURRENT_TIMESTAMP, NULL, 'Failed'),
--     ('44444444-4444-4444-4444-444444444448', '11111111-1111-1111-1111-111111111118', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Completed'),
--     ('44444444-4444-4444-4444-444444444449', '11111111-1111-1111-1111-111111111119', CURRENT_TIMESTAMP, NULL, 'Cancelled'),
--     ('44444444-4444-4444-4444-444444444450', '11111111-1111-1111-1111-111111111120', CURRENT_TIMESTAMP, NULL, 'Running');
--
-- INSERT INTO execution_logs (id, execution_id, stage_id, timestamp, message, log_level)
-- VALUES
--     ('55555555-5555-5555-5555-555555555551', '44444444-4444-4444-4444-444444444441', '22222222-2222-2222-2222-222222222221', CURRENT_TIMESTAMP, 'Started execution', 'Info'),
--     ('55555555-5555-5555-5555-555555555552', '44444444-4444-4444-4444-444444444442', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, 'Execution completed successfully', 'Info'),
--     ('55555555-5555-5555-5555-555555555553', '44444444-4444-4444-4444-444444444443', '22222222-2222-2222-2222-222222222223', CURRENT_TIMESTAMP, 'Execution failed', 'Error'),
--     ('55555555-5555-5555-5555-555555555554', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222224', CURRENT_TIMESTAMP, 'Execution running', 'Warn'),
--     ('55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444445', '22222222-2222-2222-2222-222222222225', CURRENT_TIMESTAMP, 'Execution cancelled', 'Info'),
--     ('55555555-5555-5555-5555-555555555556', '44444444-4444-4444-4444-444444444446', '22222222-2222-2222-2222-222222222226', CURRENT_TIMESTAMP, 'Execution in progress', 'Info'),
--     ('55555555-5555-5555-5555-555555555557', '44444444-4444-4444-4444-444444444447', '22222222-2222-2222-2222-222222222227', CURRENT_TIMESTAMP, 'Execution failed', 'Error'),
--     ('55555555-5555-5555-5555-555555555558', '44444444-4444-4444-4444-444444444448', '22222222-2222-2222-2222-222222222228', CURRENT_TIMESTAMP, 'Execution completed successfully', 'Info'),
--     ('55555555-5555-5555-5555-555555555559', '44444444-4444-4444-4444-444444444449', '22222222-2222-2222-2222-222222222229', CURRENT_TIMESTAMP, 'Execution cancelled', 'Warn'),
--     ('55555555-5555-5555-5555-555555555560', '44444444-4444-4444-4444-444444444450', '22222222-2222-2222-2222-222222222230', CURRENT_TIMESTAMP, 'Execution running', 'Info');
--
-- INSERT INTO schedules (id, cron_expression, timezone)
-- VALUES
--     ('66666666-6666-6666-6666-666666666661', '0 12 * * *', 'UTC'),
--     ('66666666-6666-6666-6666-666666666662', '0 6 * * 1', 'America/New_York'),
--     ('66666666-6666-6666-6666-666666666663', '30 8 * * *', 'Europe/London'),
--     ('66666666-6666-6666-6666-666666666664', '15 10 * * 2', 'Asia/Tokyo'),
--     ('66666666-6666-6666-6666-666666666665', '0 22 * * 6', 'Australia/Sydney'),
--     ('66666666-6666-6666-6666-666666666666', '0 4 * * *', 'Africa/Johannesburg'),
--     ('66666666-6666-6666-6666-666666666667', '45 14 * * 3', 'UTC'),
--     ('66666666-6666-6666-6666-666666666668', '30 9 * * *', 'America/Chicago'),
--     ('66666666-6666-6666-6666-666666666669', '0 18 * * 4', 'Asia/Dubai'),
--     ('66666666-6666-6666-6666-666666666670', '15 7 * * *', 'Europe/Paris');
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666661'
-- WHERE id = '11111111-1111-1111-1111-111111111111';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666662'
-- WHERE id = '11111111-1111-1111-1111-111111111112';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666663'
-- WHERE id = '11111111-1111-1111-1111-111111111113';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666664'
-- WHERE id = '11111111-1111-1111-1111-111111111114';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666665'
-- WHERE id = '11111111-1111-1111-1111-111111111115';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666666'
-- WHERE id = '11111111-1111-1111-1111-111111111116';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666667'
-- WHERE id = '11111111-1111-1111-1111-111111111117';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666668'
-- WHERE id = '11111111-1111-1111-1111-111111111118';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666669'
-- WHERE id = '11111111-1111-1111-1111-111111111119';
--
-- UPDATE pipelines
-- SET schedule_id = '66666666-6666-6666-6666-666666666670'
-- WHERE id = '11111111-1111-1111-1111-111111111120';