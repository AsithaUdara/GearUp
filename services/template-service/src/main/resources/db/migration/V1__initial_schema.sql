-- ========================================
-- Template Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for template service (placeholder)

-- This is a placeholder migration for template-service
-- Add your service-specific tables here as the service is implemented

-- Example table (can be modified or removed):
CREATE TABLE IF NOT EXISTS service_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL DEFAULT 0,
    duration_minutes INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_service_templates_active ON service_templates(active);
CREATE INDEX IF NOT EXISTS idx_service_templates_name ON service_templates(name);

COMMENT ON TABLE service_templates IS 'Service offering templates (admin managed)';
