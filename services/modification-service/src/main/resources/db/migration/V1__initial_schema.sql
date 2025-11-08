-- ========================================
-- Modification Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for modification service

-- Services table - stores available modification services
CREATE TABLE IF NOT EXISTS services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2),
    estimated_duration_hours INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers table - stores customer information
CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Modification requests table - stores modification requests
CREATE TABLE IF NOT EXISTS modification_requests (
    id SERIAL PRIMARY KEY,
    service_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    preferred_date DATE,
    notes TEXT,
    admin_notes TEXT,
    estimated_cost DECIMAL(10,2),
    final_cost DECIMAL(10,2),
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_modification_requests_service ON modification_requests(service_id);
CREATE INDEX IF NOT EXISTS idx_modification_requests_customer ON modification_requests(customer_id);
CREATE INDEX IF NOT EXISTS idx_modification_requests_status ON modification_requests(status);
CREATE INDEX IF NOT EXISTS idx_customers_user_id ON customers(user_id);

-- Insert sample services
INSERT INTO services (name, description, base_price, estimated_duration_hours) VALUES
('Engine Performance Upgrade', 'Enhanced engine performance modifications', 1500.00, 8),
('Suspension Modification', 'Custom suspension setup and modifications', 1200.00, 6),
('Exhaust System Upgrade', 'Performance exhaust system installation', 800.00, 4),
('Body Kit Installation', 'Custom body kit installation and fitting', 2000.00, 12),
('Interior Customization', 'Custom interior modifications and upgrades', 1000.00, 10),
('Lighting System Upgrade', 'Custom lighting system modifications', 500.00, 3),
('Wheel and Tire Package', 'Custom wheels and performance tires', 1800.00, 2);

COMMENT ON TABLE services IS 'Available modification services';
COMMENT ON TABLE customers IS 'Customer information';
COMMENT ON TABLE modification_requests IS 'Vehicle modification requests from customers';