INSERT INTO pipelines (id, name, description, schedule_id, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'Pipeline 1', 'Description for Pipeline 1', NULL, 'Active');

-- INSERT INTO connectors (id, stage_id, name, connector_type, configuration)
-- VALUES
--     ('33333333-3333-3333-3333-333333333331', NULL, 'Connector 1', 'Database', '{"db":"postgres"}'),
--     ('33333333-3333-3333-3333-333333333332', NULL, 'Connector 2', 'CloudStorage', '{"bucket":"s3"}')