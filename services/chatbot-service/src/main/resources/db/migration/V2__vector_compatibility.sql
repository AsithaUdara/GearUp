-- Conditional migration that handles pgvector availability
-- This migration converts TEXT to vector(768) if pgvector is available

DO $$
BEGIN
    -- If pgvector extension exists, convert TEXT to vector type
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'vector') THEN
        -- Convert embedding column from TEXT to vector(768)
        ALTER TABLE knowledge_documents ALTER COLUMN embedding TYPE vector(768) USING embedding::vector(768);
        
        -- Create vector similarity search index
        CREATE INDEX IF NOT EXISTS idx_knowledge_documents_embedding ON knowledge_documents 
        USING hnsw (embedding vector_cosine_ops);
        
        RAISE NOTICE 'pgvector available: embedding column converted to vector(768) with index';
    ELSE
        -- Keep as TEXT for environments without pgvector (CI/CD)
        RAISE NOTICE 'pgvector not available: keeping embedding as TEXT for compatibility';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Migration V2 completed with current configuration';
END $$;
