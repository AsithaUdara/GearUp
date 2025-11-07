-- Fix chat_sessions status check constraint to accept uppercase values from Java enums
-- This migration updates the constraint to accept both uppercase and lowercase status values

-- Drop the existing constraint
ALTER TABLE chat_sessions DROP CONSTRAINT IF EXISTS chat_sessions_status_check;

-- Add new constraint that accepts both uppercase and lowercase
ALTER TABLE chat_sessions ADD CONSTRAINT chat_sessions_status_check 
CHECK (status IN ('ACTIVE', 'CLOSED', 'ARCHIVED', 'active', 'closed', 'archived'));

-- Similarly for conversation_history sender constraint
ALTER TABLE conversation_history DROP CONSTRAINT IF EXISTS conversation_history_sender_check;

ALTER TABLE conversation_history ADD CONSTRAINT conversation_history_sender_check 
CHECK (sender IN ('USER', 'BOT', 'SYSTEM', 'user', 'bot', 'system'));

COMMENT ON CONSTRAINT chat_sessions_status_check ON chat_sessions IS 'Allows both uppercase (Java enum) and lowercase status values';
COMMENT ON CONSTRAINT conversation_history_sender_check ON conversation_history IS 'Allows both uppercase (Java enum) and lowercase sender values';
