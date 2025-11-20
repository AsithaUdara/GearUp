-- Manual fix script for pgvector embedding column
-- Run this if you're getting schema validation errors about vector(768) type

-- Step 1: Verify pgvector extension is installed
CREATE EXTENSION IF NOT EXISTS vector;

-- Step 2: Check current column type
SELECT 
    column_name, 
    data_type, 
    udt_name,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'knowledge_documents' 
AND column_name = 'embedding';

-- Step 3: If the column is TEXT, convert it to vector(768)
-- This handles both empty and populated tables
DO $$
BEGIN
    -- Check if column exists and is not already vector type
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'knowledge_documents' 
        AND column_name = 'embedding'
        AND data_type != 'USER-DEFINED'
    ) THEN
        -- Convert TEXT to vector(768)
        ALTER TABLE knowledge_documents 
        ALTER COLUMN embedding TYPE vector(768) 
        USING CASE 
            WHEN embedding IS NULL THEN NULL
            WHEN embedding = '' THEN NULL
            ELSE embedding::vector(768)
        END;
        
        RAISE NOTICE 'Successfully converted embedding column to vector(768)';
        
        -- Create or replace the vector index for similarity search
        DROP INDEX IF EXISTS idx_knowledge_documents_embedding;
        CREATE INDEX idx_knowledge_documents_embedding ON knowledge_documents 
        USING hnsw (embedding vector_cosine_ops);
        
        RAISE NOTICE 'Created HNSW index for vector similarity search';
    ELSE
        RAISE NOTICE 'Column embedding is already vector type or does not exist';
    END IF;
END $$;

-- Step 4: Verify the change
SELECT 
    column_name, 
    data_type, 
    udt_name
FROM information_schema.columns 
WHERE table_name = 'knowledge_documents' 
AND column_name = 'embedding';

-- Step 5: Check if index exists
SELECT 
    indexname, 
    indexdef
FROM pg_indexes 
WHERE tablename = 'knowledge_documents' 
AND indexname = 'idx_knowledge_documents_embedding';
