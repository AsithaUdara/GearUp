-- ========================================
-- Analytical Service - Schema Update
-- ========================================
-- Migration: V2__update_schema_for_dashboard.sql
-- Description: Updates tables to match dashboard requirements

-- Drop existing tables that don't match our new structure
DROP TABLE IF EXISTS service_analytics CASCADE;
DROP TABLE IF EXISTS revenue_analytics CASCADE;
DROP TABLE IF EXISTS employee_performance CASCADE;
DROP TABLE IF EXISTS customer_analytics CASCADE;
DROP TABLE IF EXISTS popular_services CASCADE;

-- Recreate Service Analytics Table with new structure
CREATE TABLE IF NOT EXISTS service_analytics (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    service_type VARCHAR(100),
    appointment_count INTEGER NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10, 2),
    average_rating DECIMAL(3, 2),
    record_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recreate Revenue Analytics Table
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

-- Recreate Employee Performance Table
CREATE TABLE IF NOT EXISTS employee_performance (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    tasks_completed INTEGER DEFAULT 0,
    total_service_time INTEGER DEFAULT 0,
    average_rating DECIMAL(3, 2),
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, date)
);

-- Recreate Customer Analytics Table with new structure
CREATE TABLE IF NOT EXISTS customer_analytics (
    id BIGSERIAL PRIMARY KEY,
    new_customers INTEGER NOT NULL DEFAULT 0,
    returning_customers INTEGER DEFAULT 0,
    total_customers INTEGER DEFAULT 0,
    customer_retention_rate DECIMAL(5, 2),
    record_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recreate Popular Services Table with new structure
CREATE TABLE IF NOT EXISTS popular_services (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    booking_count INTEGER NOT NULL DEFAULT 0,
    percentage_of_total DECIMAL(5, 2),
    rank_position INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Recent Activity Table
CREATE TABLE IF NOT EXISTS recent_activity (
    id BIGSERIAL PRIMARY KEY,
    event_description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OK', 'ATTENTION', 'WARNING')),
    event_timestamp TIMESTAMP NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_service_analytics_date ON service_analytics(record_date);
CREATE INDEX idx_service_analytics_name ON service_analytics(service_name);
CREATE INDEX idx_revenue_analytics_date ON revenue_analytics(date);
CREATE INDEX idx_employee_performance_employee_date ON employee_performance(employee_id, date);
CREATE INDEX idx_customer_analytics_date ON customer_analytics(record_date);
CREATE INDEX idx_popular_services_rank ON popular_services(rank_position);
CREATE INDEX idx_recent_activity_timestamp ON recent_activity(event_timestamp DESC);
CREATE INDEX idx_recent_activity_status ON recent_activity(status);

-- Add comments for documentation
COMMENT ON TABLE service_analytics IS 'Service performance analytics grouped by date';
COMMENT ON TABLE revenue_analytics IS 'Daily revenue and business metrics';
COMMENT ON TABLE employee_performance IS 'Employee performance metrics and KPIs';
COMMENT ON TABLE customer_analytics IS 'Customer growth and retention analytics';
COMMENT ON TABLE popular_services IS 'Most popular services by booking count';
COMMENT ON TABLE recent_activity IS 'Recent system activities and events';
