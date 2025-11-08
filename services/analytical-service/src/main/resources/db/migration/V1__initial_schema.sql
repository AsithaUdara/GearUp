-- ========================================
-- Analytical Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for analytical service

-- Service Analytics Table
CREATE TABLE IF NOT EXISTS service_analytics (
    id BIGSERIAL PRIMARY KEY,
    service_id VARCHAR(50) NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    vehicle_model VARCHAR(100),
    completion_time INTEGER, -- in minutes
    employee_id VARCHAR(100),
    customer_rating INTEGER CHECK (customer_rating BETWEEN 1 AND 5),
    total_cost DECIMAL(10, 2),
    parts_cost DECIMAL(10, 2),
    labor_cost DECIMAL(10, 2),
    service_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Revenue Analytics Table
CREATE TABLE IF NOT EXISTS revenue_analytics (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_revenue DECIMAL(12, 2) DEFAULT 0,
    service_revenue DECIMAL(12, 2) DEFAULT 0,
    parts_revenue DECIMAL(12, 2) DEFAULT 0,
    total_services INTEGER DEFAULT 0,
    total_customers INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employee Performance Table
CREATE TABLE IF NOT EXISTS employee_performance (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    tasks_completed INTEGER DEFAULT 0,
    total_service_time INTEGER DEFAULT 0, -- in minutes
    average_rating DECIMAL(3, 2),
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, date)
);

-- Customer Analytics Table
CREATE TABLE IF NOT EXISTS customer_analytics (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL UNIQUE,
    total_services INTEGER DEFAULT 0,
    total_spent DECIMAL(12, 2) DEFAULT 0,
    average_service_rating DECIMAL(3, 2),
    last_service_date DATE,
    first_service_date DATE,
    loyalty_points INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Popular Services Table (for trending analysis)
CREATE TABLE IF NOT EXISTS popular_services (
    id BIGSERIAL PRIMARY KEY,
    service_type VARCHAR(100) NOT NULL UNIQUE,
    total_bookings INTEGER DEFAULT 0,
    total_revenue DECIMAL(12, 2) DEFAULT 0,
    average_rating DECIMAL(3, 2),
    average_completion_time INTEGER, -- in minutes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_service_analytics_date ON service_analytics(service_date);
CREATE INDEX idx_service_analytics_employee ON service_analytics(employee_id);
CREATE INDEX idx_service_analytics_type ON service_analytics(service_type);
CREATE INDEX idx_revenue_analytics_date ON revenue_analytics(date);
CREATE INDEX idx_employee_performance_employee_date ON employee_performance(employee_id, date);
CREATE INDEX idx_customer_analytics_customer ON customer_analytics(customer_id);

-- Add comments for documentation
COMMENT ON TABLE service_analytics IS 'Detailed analytics for each service performed';
COMMENT ON TABLE revenue_analytics IS 'Daily revenue and business metrics';
COMMENT ON TABLE employee_performance IS 'Employee performance metrics and KPIs';
COMMENT ON TABLE customer_analytics IS 'Customer behavior and lifetime value analytics';
COMMENT ON TABLE popular_services IS 'Trending services and popularity metrics';
