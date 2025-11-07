-- ========================================
-- GearUp Backend - PostgreSQL Initialization
-- ========================================
-- This script creates separate databases and users for each microservice
-- Following microservices architecture best practices
-- 
-- NOTE: Flyway migrations handle table creation
-- This script only creates databases, users, and grants permissions

-- Create databases for each microservice
CREATE DATABASE as_automobile_service;
CREATE DATABASE as_notification_service;
CREATE DATABASE as_user_auth_service;
CREATE DATABASE as_template_service;
CREATE DATABASE as_chatbot_service;
CREATE DATABASE as_vehicle_service;
CREATE DATABASE as_customer_service;
CREATE DATABASE as_tracking_service;
CREATE DATABASE as_analytical_service;

-- Create dedicated service users with strong passwords from environment variables
-- Docker will pass these via POSTGRES_INITDB_ARGS
CREATE USER svc_automobile_service WITH PASSWORD :'AUTOMOBILE_DB_PASSWORD';
CREATE USER svc_notification_service WITH PASSWORD :'NOTIFICATION_DB_PASSWORD';
CREATE USER svc_user_auth_service WITH PASSWORD :'USER_AUTH_DB_PASSWORD';
CREATE USER svc_template_service WITH PASSWORD :'TEMPLATE_DB_PASSWORD';
CREATE USER svc_chatbot_service WITH PASSWORD :'CHATBOT_DB_PASSWORD';
CREATE USER svc_vehicle_service WITH PASSWORD :'VEHICLE_DB_PASSWORD';
CREATE USER svc_customer_service WITH PASSWORD :'CUSTOMER_DB_PASSWORD';
CREATE USER svc_tracking_service WITH PASSWORD 'tracking_svc_pass_2024';
CREATE USER svc_analytical_service WITH PASSWORD 'analytical_svc_pass_2024';

-- Grant all privileges on respective databases to service users
GRANT ALL PRIVILEGES ON DATABASE as_automobile_service TO svc_automobile_service;
GRANT ALL PRIVILEGES ON DATABASE as_notification_service TO svc_notification_service;
GRANT ALL PRIVILEGES ON DATABASE as_user_auth_service TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON DATABASE as_template_service TO svc_template_service;
GRANT ALL PRIVILEGES ON DATABASE as_chatbot_service TO svc_chatbot_service;
GRANT ALL PRIVILEGES ON DATABASE as_vehicle_service TO svc_vehicle_service;
GRANT ALL PRIVILEGES ON DATABASE as_customer_service TO svc_customer_service;
GRANT ALL PRIVILEGES ON DATABASE as_tracking_service TO svc_tracking_service;
GRANT ALL PRIVILEGES ON DATABASE as_analytical_service TO svc_analytical_service;

-- ========================================
-- Automobile Service Database Setup
-- ========================================
\c as_automobile_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_automobile_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_automobile_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_automobile_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_automobile_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_automobile_service;

-- ========================================
-- Notification Service Database Setup
-- ========================================
\c as_notification_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_notification_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_notification_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_notification_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_notification_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_notification_service;

-- ========================================
-- User Auth Service Database Setup
-- ========================================
\c as_user_auth_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_user_auth_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_user_auth_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_user_auth_service;

-- ========================================
-- Template Service Database Setup
-- ========================================
\c as_template_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_template_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_template_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_template_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_template_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_template_service;

-- ========================================
-- Chatbot Service Database Setup
-- ========================================
\c as_chatbot_service;

-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_chatbot_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_chatbot_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_chatbot_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_chatbot_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_chatbot_service;

-- ========================================
-- Vehicle Service Database Setup
-- ========================================
\c as_vehicle_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_vehicle_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_vehicle_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_vehicle_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_vehicle_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_vehicle_service;

-- ========================================
-- Customer Service Database Setup
-- ========================================
\c as_customer_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_customer_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_customer_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_customer_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_customer_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_customer_service;

-- ========================================
-- Tracking Service Database Setup
-- ========================================
\c as_tracking_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_tracking_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_tracking_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_tracking_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_tracking_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_tracking_service;

-- ========================================
-- Analytical Service Database Setup
-- ========================================
\c as_analytical_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_analytical_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_analytical_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_analytical_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_analytical_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_analytical_service;

-- Log completion
\c postgres;
SELECT 'PostgreSQL initialization completed successfully! Run Flyway migrations to create tables.' AS status;
