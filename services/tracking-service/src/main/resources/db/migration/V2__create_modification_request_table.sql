-- Create modification_request table
CREATE TABLE IF NOT EXISTS modification_request (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(50) UNIQUE NOT NULL,
    service_id VARCHAR(50) NOT NULL,
    vehicle VARCHAR(100) NOT NULL,
    customer VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('add_service', 'remove_service', 'change_service', 'urgent_repair')),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'approved', 'rejected', 'in_progress', 'completed')),
    requested_by VARCHAR(100) NOT NULL,
    assigned_to_employee_id VARCHAR(100),
    estimated_cost DECIMAL(10, 2),
    estimated_duration INTEGER,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_modification_request_vehicle ON modification_request(vehicle);
CREATE INDEX idx_modification_request_status ON modification_request(status);
CREATE INDEX idx_modification_request_assigned ON modification_request(assigned_to_employee_id);
CREATE INDEX idx_modification_request_service_id ON modification_request(service_id);
CREATE INDEX idx_modification_request_requested_by ON modification_request(requested_by);

-- Add comments
COMMENT ON TABLE modification_request IS 'Customer service modification and change requests';
COMMENT ON COLUMN modification_request.requested_by IS 'Customer Firebase UID';
COMMENT ON COLUMN modification_request.assigned_to_employee_id IS 'Assigned employee Firebase UID';
COMMENT ON COLUMN modification_request.estimated_duration IS 'Duration in minutes';
