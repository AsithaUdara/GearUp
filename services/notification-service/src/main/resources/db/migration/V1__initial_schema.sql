-- ========================================
-- Notification Service - Initial Schema
-- ========================================
-- Migration: V1__initial_schema.sql
-- Description: Creates initial tables for notification service

-- Create notifications table
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

-- Create indexes for performance
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE notifications IS 'Stores notification records for all users';
COMMENT ON COLUMN notifications.type IS 'Notification type: EMAIL, SMS, PUSH, IN_APP, etc.';
COMMENT ON COLUMN notifications.status IS 'Notification status: PENDING, SENT, FAILED, etc.';
COMMENT ON COLUMN notifications.metadata IS 'Additional metadata in JSON format';
