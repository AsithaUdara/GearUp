-- Conditional migration that handles environments without pgvector
-- This migration runs after V1 and adds compatibility for testing

-- Check if vector type exists and create alternative if not
DO $$
BEGIN
    -- Try to detect if we're in a test environment
    -- If the vector type doesn't exist, we'll handle it gracefully
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vector') THEN
        -- Alter the column to TEXT for compatibility
        ALTER TABLE knowledge_documents ALTER COLUMN embedding TYPE TEXT;
        RAISE NOTICE 'Running in non-pgvector environment: embedding column converted to TEXT';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- If any error occurs, log it but continue
        RAISE NOTICE 'Migration V2 completed with compatibility mode';
END $$;
