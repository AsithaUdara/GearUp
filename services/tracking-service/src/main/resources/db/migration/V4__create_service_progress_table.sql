-- Create service_progress table
CREATE TABLE IF NOT EXISTS service_progress (
    id BIGSERIAL PRIMARY KEY,
    progress_id VARCHAR(50) UNIQUE NOT NULL,
    service_id VARCHAR(50) NOT NULL UNIQUE,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_year VARCHAR(10),
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20),
    customer_email VARCHAR(100),
    appointment_date DATE,
    appointment_time VARCHAR(20),
    estimated_completion VARCHAR(50),
    current_step INTEGER DEFAULT 0 CHECK (current_step >= 0),
    total_steps INTEGER DEFAULT 5 CHECK (total_steps > 0),
    overall_status VARCHAR(20) CHECK (overall_status IN ('scheduled', 'in_progress', 'completed', 'delayed')),
    technician_id VARCHAR(100),
    technician_name VARCHAR(100),
    location_name VARCHAR(200),
    last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_service_progress_customer ON service_progress(customer_name);
CREATE INDEX idx_service_progress_service_id ON service_progress(service_id);
CREATE INDEX idx_service_progress_technician ON service_progress(technician_id);
CREATE INDEX idx_service_progress_status ON service_progress(overall_status);
CREATE INDEX idx_service_progress_appointment_date ON service_progress(appointment_date);

-- Add comments
COMMENT ON TABLE service_progress IS 'Customer-facing service progress tracking';
COMMENT ON COLUMN service_progress.technician_id IS 'Technician Firebase UID';
COMMENT ON COLUMN service_progress.current_step IS 'Current progress step (0-based)';
COMMENT ON COLUMN service_progress.total_steps IS 'Total number of steps for completion';
