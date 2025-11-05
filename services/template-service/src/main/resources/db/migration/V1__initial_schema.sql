-- ========================================
-- Template Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for template service (placeholder)

-- This is a placeholder migration for template-service
-- Add your service-specific tables here as the service is implemented

-- Example table (can be modified or removed):
CREATE TABLE IF NOT EXISTS templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE templates IS 'Placeholder table for template service - modify as needed';
