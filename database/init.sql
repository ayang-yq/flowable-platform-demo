-- Flowable Platform Database Initialization
-- This script is executed on PostgreSQL container startup

\echo 'Initializing Flowable Platform database...'

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set default schema
SET search_path TO public;

\echo 'Database initialization complete.'
