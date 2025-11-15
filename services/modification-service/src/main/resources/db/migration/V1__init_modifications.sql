CREATE TABLE IF NOT EXISTS modifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(64) NOT NULL,
    vehicle_id VARCHAR(255) NOT NULL,
    vehicle_label VARCHAR(500),
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_status CHECK (status IN ('pending', 'approved', 'in_progress', 'completed', 'rejected'))
);

-- Indexes
CREATE INDEX idx_modifications_user_id ON modifications(user_id);
CREATE INDEX idx_modifications_vehicle_id ON modifications(vehicle_id);
CREATE INDEX idx_modifications_status ON modifications(status);
CREATE INDEX idx_modifications_created_at ON modifications(created_at DESC);

-- EOF: Flyway migration V1 for modifications table
