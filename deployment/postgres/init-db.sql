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
CREATE DATABASE as_appointment_service;
CREATE DATABASE as_modification_service;

-- Create dedicated service users with strong passwords from environment variables
-- Docker will pass these via POSTGRES_INITDB_ARGS
CREATE USER svc_automobile_service WITH PASSWORD :'AUTOMOBILE_DB_PASSWORD';
CREATE USER svc_notification_service WITH PASSWORD :'NOTIFICATION_DB_PASSWORD';
CREATE USER svc_user_auth_service WITH PASSWORD :'USER_AUTH_DB_PASSWORD';
CREATE USER svc_template_service WITH PASSWORD :'TEMPLATE_DB_PASSWORD';
CREATE USER svc_appointment_service WITH PASSWORD :'APPOINTMENT_DB_PASSWORD';
CREATE USER svc_modification_service WITH PASSWORD :'MODIFICATION_DB_PASSWORD';

-- Grant all privileges on respective databases to service users
GRANT ALL PRIVILEGES ON DATABASE as_automobile_service TO svc_automobile_service;
GRANT ALL PRIVILEGES ON DATABASE as_notification_service TO svc_notification_service;
GRANT ALL PRIVILEGES ON DATABASE as_user_auth_service TO svc_user_auth_service;
GRANT ALL PRIVILEGES ON DATABASE as_template_service TO svc_template_service;
GRANT ALL PRIVILEGES ON DATABASE as_appointment_service TO svc_appointment_service;
GRANT ALL PRIVILEGES ON DATABASE as_modification_service TO svc_modification_service;

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
-- Appointment Service Database Setup
-- ========================================
\c as_appointment_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_appointment_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_appointment_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_appointment_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_appointment_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_appointment_service;

-- ========================================
-- Modification Service Database Setup
-- ========================================
\c as_modification_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO svc_modification_service;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_modification_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_modification_service;

-- Set default privileges for future tables (created by Flyway)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_modification_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_modification_service;

-- Log completion
\c postgres;
SELECT 'PostgreSQL initialization completed successfully! Run Flyway migrations to create tables.' AS status;
