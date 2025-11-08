#!/bin/bash
set -e

# This script runs during PostgreSQL container initialization
# Environment variables are passed from docker-compose

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create databases for each service
    CREATE DATABASE as_automobile_service;
    CREATE DATABASE as_notification_service;
    CREATE DATABASE as_user_auth_service;
    CREATE DATABASE as_chatbot_service;
    CREATE DATABASE as_customer_service;
    CREATE DATABASE as_vehicle_service;
    CREATE DATABASE as_analytical_service;
    CREATE DATABASE as_tracking_service;
    CREATE DATABASE as_payment_service;
    CREATE DATABASE as_parts_service;

    -- Create dedicated service users with passwords from environment
    CREATE USER svc_automobile_service WITH PASSWORD '$AUTOMOBILE_DB_PASSWORD';
    CREATE USER svc_notification_service WITH PASSWORD '$NOTIFICATION_DB_PASSWORD';
    CREATE USER svc_user_auth_service WITH PASSWORD '$USER_AUTH_DB_PASSWORD';
    CREATE USER svc_chatbot_service WITH PASSWORD '$CHATBOT_DB_PASSWORD';
    CREATE USER svc_customer_service WITH PASSWORD '$CUSTOMER_DB_PASSWORD';
    CREATE USER svc_vehicle_service WITH PASSWORD '$VEHICLE_DB_PASSWORD';
    CREATE USER svc_analytical_service WITH PASSWORD '$ANALYTICAL_DB_PASSWORD';
    CREATE USER svc_tracking_service WITH PASSWORD '$TRACKING_DB_PASSWORD';
    CREATE USER svc_payment_service WITH PASSWORD '$PAYMENT_DB_PASSWORD';
    CREATE USER svc_parts_service WITH PASSWORD '$PARTS_DB_PASSWORD';

    -- Grant all privileges on respective databases to service users
    GRANT ALL PRIVILEGES ON DATABASE as_automobile_service TO svc_automobile_service;
    GRANT ALL PRIVILEGES ON DATABASE as_notification_service TO svc_notification_service;
    GRANT ALL PRIVILEGES ON DATABASE as_user_auth_service TO svc_user_auth_service;
    GRANT ALL PRIVILEGES ON DATABASE as_chatbot_service TO svc_chatbot_service;
    GRANT ALL PRIVILEGES ON DATABASE as_customer_service TO svc_customer_service;
    GRANT ALL PRIVILEGES ON DATABASE as_vehicle_service TO svc_vehicle_service;
    GRANT ALL PRIVILEGES ON DATABASE as_analytical_service TO svc_analytical_service;
    GRANT ALL PRIVILEGES ON DATABASE as_tracking_service TO svc_tracking_service;
    GRANT ALL PRIVILEGES ON DATABASE as_payment_service TO svc_payment_service;
    GRANT ALL PRIVILEGES ON DATABASE as_parts_service TO svc_parts_service;
EOSQL

# Setup pgvector extension and permissions for automobile service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_automobile_service" <<-EOSQL
    -- Create pgvector extension
    CREATE EXTENSION IF NOT EXISTS vector;

    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_automobile_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_automobile_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_automobile_service;

    -- Grant default privileges for future objects
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_automobile_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_automobile_service;
EOSQL

# Setup notification service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_notification_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_notification_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_notification_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_notification_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_notification_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_notification_service;
EOSQL

# Setup user auth service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_user_auth_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_user_auth_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_user_auth_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_user_auth_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_user_auth_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_user_auth_service;
EOSQL

# Setup chatbot service database with pgvector
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_chatbot_service" <<-EOSQL
    -- Create pgvector extension for vector embeddings
    CREATE EXTENSION IF NOT EXISTS vector;

    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_chatbot_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_chatbot_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_chatbot_service;

    -- Grant default privileges for future objects
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_chatbot_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_chatbot_service;
EOSQL

# Setup customer service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_customer_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_customer_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_customer_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_customer_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_customer_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_customer_service;
EOSQL

# Setup vehicle service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_vehicle_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_vehicle_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_vehicle_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_vehicle_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_vehicle_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_vehicle_service;
EOSQL

# Setup analytical service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_analytical_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_analytical_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_analytical_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_analytical_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_analytical_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_analytical_service;
EOSQL

# Setup tracking service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_tracking_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_tracking_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_tracking_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_tracking_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_tracking_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_tracking_service;
EOSQL

# Setup payment service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_payment_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_payment_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_payment_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_payment_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_payment_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_payment_service;
EOSQL

# Setup parts service database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "as_parts_service" <<-EOSQL
    -- Grant schema permissions
    GRANT ALL ON SCHEMA public TO svc_parts_service;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO svc_parts_service;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO svc_parts_service;

    -- Grant default privileges
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_parts_service;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_parts_service;
EOSQL

echo "Database initialization completed successfully!"
