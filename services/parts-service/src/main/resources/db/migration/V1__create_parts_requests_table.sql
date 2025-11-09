-- ========================================
-- Parts Service - Initial Schema
-- ========================================
-- Version: V1
-- Description: Creates table for parts and materials requests

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- Table: parts_requests
-- ========================================
CREATE TABLE parts_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id VARCHAR(20) NOT NULL UNIQUE,
    material VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    notes TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for parts_requests
CREATE INDEX idx_parts_requests_status ON parts_requests(status);
CREATE INDEX idx_parts_requests_created_by ON parts_requests(created_by);
CREATE INDEX idx_parts_requests_created_at ON parts_requests(created_at);
CREATE INDEX idx_parts_requests_request_id ON parts_requests(request_id);

-- ========================================
-- Trigger: Update updated_at timestamp
-- ========================================
CREATE OR REPLACE FUNCTION update_parts_requests_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_parts_requests_updated_at BEFORE UPDATE
    ON parts_requests FOR EACH ROW
    EXECUTE FUNCTION update_parts_requests_updated_at();

-- ========================================
-- Comments
-- ========================================
COMMENT ON TABLE parts_requests IS 'Stores parts and materials requests from technicians/employees';
COMMENT ON COLUMN parts_requests.request_id IS 'Unique human-readable request identifier';
COMMENT ON COLUMN parts_requests.material IS 'Name/description of the requested part or material';
COMMENT ON COLUMN parts_requests.quantity IS 'Quantity of parts/materials requested';
COMMENT ON COLUMN parts_requests.status IS 'Request status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN parts_requests.created_by IS 'UUID of the user who created the request';
