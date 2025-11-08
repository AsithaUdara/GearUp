-- ========================================
-- Appointment Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for appointment service

-- Services table - stores available services for appointments
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL DEFAULT 60,
    price DECIMAL(10,2),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Time slots table - stores available time slots
CREATE TABLE IF NOT EXISTS time_slots (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- Bookings table - stores appointment bookings
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    notes TEXT,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    role VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees (name, email, phone, role)
VALUES
('Aarav Gupta',   'aarav.g@example.com', '9001112222', 'Service Advisor'),
('Priya Shah',    'priya.s@example.com', '9003334444', 'Technician'),
('Rohan Mehta',   'rohan.m@example.com', '9005556666', 'Technician'),
('Sneha Iyer',    'sneha.i@example.com', '9007778888', 'Service Advisor'),
('Dev Patel',     'dev.p@example.com',   '9009990000', 'Technician');

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_time_slots_service_date ON time_slots(service_id, slot_date);
CREATE INDEX IF NOT EXISTS idx_time_slots_available ON time_slots(is_available);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_service ON bookings(service_id);

-- Insert sample services
INSERT INTO services (name, description, duration_minutes, price) VALUES
('Oil Change', 'Complete engine oil change service', 30, 29.99),
('Tire Rotation', 'Rotate tires for even wear', 45, 39.99),
('Brake Inspection', 'Complete brake system inspection', 60, 49.99),
('Battery Check', 'Battery health and charging system check', 30, 19.99),
('General Inspection', 'Comprehensive vehicle inspection', 90, 79.99);

COMMENT ON TABLE services IS 'Available services for appointment booking';
COMMENT ON TABLE time_slots IS 'Available time slots for each service';
COMMENT ON TABLE bookings IS 'Customer appointment bookings';
