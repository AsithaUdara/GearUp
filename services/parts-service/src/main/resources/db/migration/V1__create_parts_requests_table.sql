CREATE TABLE parts_requests (
    id UUID PRIMARY KEY,
    request_id VARCHAR(20) NOT NULL UNIQUE,
    material VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT status_check CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);