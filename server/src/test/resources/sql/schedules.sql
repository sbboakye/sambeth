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

-- -- Insert 1
-- INSERT INTO schedules (cron_expression, timezone)
-- VALUES ('0 0 12 * * ?', 'UTC');
--
-- -- Insert 2
-- INSERT INTO schedules (cron_expression, timezone)
-- VALUES ('0 30 9 ? * MON-FRI', 'America/New_York');
--
-- -- Insert 3
-- INSERT INTO schedules (cron_expression, timezone)
-- VALUES ('0 0/15 8-17 * * ?', 'Asia/Tokyo');
--
-- -- Insert 4
-- INSERT INTO schedules (cron_expression, timezone)
-- VALUES ('0 0 1 1 * ?', 'Europe/London');
--
-- -- Insert 5
-- INSERT INTO schedules (cron_expression, timezone)
-- VALUES ('0 0 0 25 12 ?', 'Australia/Sydney');
