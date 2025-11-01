-- V1__init.sql
-- Initial schema for template-service example

CREATE TABLE IF NOT EXISTS sample_entity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Add an index example
CREATE INDEX IF NOT EXISTS idx_sample_entity_name ON sample_entity(name);
