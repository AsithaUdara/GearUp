-- ========================================
-- Automobile Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for automobile service

-- Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    vin VARCHAR(17) UNIQUE NOT NULL,
    license_plate VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_vehicles_vin ON vehicles(vin);
CREATE INDEX idx_vehicles_status ON vehicles(status);

-- Add comments for documentation
COMMENT ON TABLE vehicles IS 'Stores vehicle information for the automobile service';
COMMENT ON COLUMN vehicles.vin IS 'Vehicle Identification Number (unique)';
COMMENT ON COLUMN vehicles.status IS 'Vehicle status: ACTIVE, INACTIVE, MAINTENANCE, etc.';
