-- Create required PostgreSQL extensions for migrations
-- This migration should run before other migrations that depend on extensions

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
