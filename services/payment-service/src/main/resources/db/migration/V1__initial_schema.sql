-- ========================================
-- Payment Service - Initial Schema
-- ========================================
-- Version: V1
-- Description: Creates tables for payment requests, service items, and customer bills

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- Table: payment_requests
-- ========================================
CREATE TABLE payment_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    vehicle_info VARCHAR(500) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    submitted_by VARCHAR(255) NOT NULL,
    submitted_date DATE NOT NULL,
    approved_date DATE,
    rejected_date DATE,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for payment_requests
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_payment_requests_customer_email ON payment_requests(customer_email);
CREATE INDEX idx_payment_requests_submitted_date ON payment_requests(submitted_date);
CREATE INDEX idx_payment_requests_approved_date ON payment_requests(approved_date);

-- ========================================
-- Table: payment_request_services
-- ========================================
CREATE TABLE payment_request_services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_request_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_request_id) REFERENCES payment_requests(id) ON DELETE CASCADE
);

-- Index for payment_request_services
CREATE INDEX idx_payment_request_services_request_id ON payment_request_services(payment_request_id);

-- ========================================
-- Table: customer_bills
-- ========================================
CREATE TABLE customer_bills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_request_id UUID NOT NULL UNIQUE,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    vehicle_info VARCHAR(500) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    tax_amount DECIMAL(10, 2) NOT NULL CHECK (tax_amount >= 0),
    final_amount DECIMAL(10, 2) NOT NULL CHECK (final_amount >= 0),
    approved_date DATE NOT NULL,
    payment_status VARCHAR(50) NOT NULL CHECK (payment_status IN ('UNPAID', 'PAID')),
    paid_date DATE,
    review_submitted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_request_id) REFERENCES payment_requests(id) ON DELETE CASCADE
);

-- Indexes for customer_bills
CREATE INDEX idx_customer_bills_customer_email ON customer_bills(customer_email);
CREATE INDEX idx_customer_bills_payment_status ON customer_bills(payment_status);
CREATE INDEX idx_customer_bills_approved_date ON customer_bills(approved_date);
CREATE INDEX idx_customer_bills_payment_request_id ON customer_bills(payment_request_id);

-- ========================================
-- Trigger: Update updated_at timestamp
-- ========================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_payment_requests_updated_at BEFORE UPDATE
    ON payment_requests FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customer_bills_updated_at BEFORE UPDATE
    ON customer_bills FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- Comments
-- ========================================
COMMENT ON TABLE payment_requests IS 'Stores payment requests submitted by employees/technicians';
COMMENT ON TABLE payment_request_services IS 'Stores individual service items for each payment request';
COMMENT ON TABLE customer_bills IS 'Stores approved bills for customers to view and pay';

COMMENT ON COLUMN payment_requests.status IS 'Payment request status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN customer_bills.payment_status IS 'Bill payment status: UNPAID or PAID';
COMMENT ON COLUMN customer_bills.review_submitted IS 'Whether customer has submitted a review for this service';
