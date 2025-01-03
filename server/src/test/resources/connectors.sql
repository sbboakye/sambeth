INSERT INTO pipelines (id, name, description, schedule_id, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'Pipeline 1', 'Description for Pipeline 1', NULL, 'Active');

INSERT INTO stages (id, pipeline_id, stage_type, configuration, position)
VALUES ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111', 'Source', '{"key":"value1"}', 1);