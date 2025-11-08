-- Create parts_request table
CREATE TABLE IF NOT EXISTS parts_request (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(50) UNIQUE NOT NULL,
    material VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Approved', 'Rejected')),
    date DATE NOT NULL,
    vehicle VARCHAR(100),
    service_id VARCHAR(50),
    requested_by VARCHAR(100),
    notes TEXT,
    cost DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_parts_request_vehicle ON parts_request(vehicle);
CREATE INDEX idx_parts_request_service_id ON parts_request(service_id);
CREATE INDEX idx_parts_request_status ON parts_request(status);
CREATE INDEX idx_parts_request_requested_by ON parts_request(requested_by);
CREATE INDEX idx_parts_request_date ON parts_request(date);

-- Add comments
COMMENT ON TABLE parts_request IS 'Parts and materials requests for services';
COMMENT ON COLUMN parts_request.requested_by IS 'Employee Firebase UID who requested';
COMMENT ON COLUMN parts_request.cost IS 'Cost of parts in LKR';
