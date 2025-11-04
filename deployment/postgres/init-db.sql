-- ========================================
-- GearUp Backend - PostgreSQL Initialization
-- ========================================
-- This script creates separate databases and users for each microservice
-- Following microservices architecture best practices

-- Create databases for each microservice
CREATE DATABASE as_automobile_service;
CREATE DATABASE as_notification_service;
CREATE DATABASE as_user_auth_service;

-- Create dedicated service users with strong passwords
CREATE USER svc_automobile_service WITH PASSWORD 'auto_svc_pass_2024';
CREATE USER svc_notification_service WITH PASSWORD 'notif_svc_pass_2024';
CREATE USER svc_user_auth_service WITH PASSWORD 'auth_svc_pass_2024';

-- Grant all privileges on respective databases to service users
GRANT ALL PRIVILEGES ON DATABASE as_automobile_service TO svc_automobile_service;
GRANT ALL PRIVILEGES ON DATABASE as_notification_service TO svc_notification_service;
GRANT ALL PRIVILEGES ON DATABASE as_user_auth_service TO svc_user_auth_service;

-- ========================================
-- Automobile Service Database Setup
-- ========================================
\c as_automobile_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_automobile_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_automobile_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_automobile_service;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_automobile_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_automobile_service;

-- Create initial tables for automobile service
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    vin VARCHAR(17) UNIQUE NOT NULL,
    license_plate VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vehicles_vin ON vehicles(vin);
CREATE INDEX idx_vehicles_status ON vehicles(status);

-- ========================================
-- Notification Service Database Setup
-- ========================================
\c as_notification_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_notification_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_notification_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_notification_service;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_notification_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_notification_service;

-- Create initial tables for notification service
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- ========================================
-- User Auth Service Database Setup
-- ========================================
\c as_user_auth_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_user_auth_service;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_user_auth_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_user_auth_service;

-- Create initial tables for user auth service
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) DEFAULT 'USER',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Log completion
\c postgres;
SELECT 'PostgreSQL initialization completed successfully!' AS status;
