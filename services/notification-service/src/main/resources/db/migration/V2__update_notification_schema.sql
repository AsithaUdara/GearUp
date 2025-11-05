-- ========================================
-- Notification Service - Update Schema
-- Migration: V2__update_notification_schema.sql
-- Description: Rename `notifications` to `notification`, evolve columns to new schema,
--              create `notification_template` table, and add indexes/sequences as required.
-- ========================================

BEGIN;

-- Drop old indexes if they exist (created by V1)
DROP INDEX IF EXISTS idx_notifications_user_id;
DROP INDEX IF EXISTS idx_notifications_status;
DROP INDEX IF EXISTS idx_notifications_type;
DROP INDEX IF EXISTS idx_notifications_created_at;

-- If the old table exists, rename it to the new plural/singular name
-- and adapt columns. If it does not exist, the following commands that
-- reference the table will be skipped by conditional checks where possible.
DO $$
BEGIN
	IF to_regclass('public.notifications') IS NOT NULL THEN
		ALTER TABLE public.notifications RENAME TO notification;
	END IF;
END$$;

-- If table exists, rename primary key column and ensure bigint identity/sequence
DO $$
BEGIN
	IF to_regclass('public.notification') IS NOT NULL AND
		 (SELECT column_name FROM information_schema.columns WHERE table_name='notification' AND column_name='id') IS NOT NULL THEN
		ALTER TABLE notification RENAME COLUMN id TO notification_id;
	END IF;
END$$;

-- Create a sequence for notification_id and set defaults so new inserts use it.
DO $$
DECLARE
	max_id bigint := 0;
BEGIN
	IF to_regclass('public.notification') IS NOT NULL THEN
		EXECUTE 'SELECT COALESCE(MAX(notification_id),0) FROM notification' INTO max_id;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relkind='S' AND relname='notification_notification_id_seq') THEN
		CREATE SEQUENCE notification_notification_id_seq;
	END IF;

	IF max_id = 0 THEN
		PERFORM setval('notification_notification_id_seq', 1, false);
	ELSE
		PERFORM setval('notification_notification_id_seq', max_id, true);
	END IF;

	-- Attach default to column if it exists
	IF to_regclass('public.notification') IS NOT NULL AND
		 (SELECT column_name FROM information_schema.columns WHERE table_name='notification' AND column_name='notification_id') IS NOT NULL THEN
		ALTER TABLE notification ALTER COLUMN notification_id SET DEFAULT nextval('notification_notification_id_seq');
		ALTER TABLE notification ALTER COLUMN notification_id TYPE bigint;
	END IF;
END$$;

-- Convert user_id to varchar(128) (previously integer) and make not null
DO $$
BEGIN
	IF to_regclass('public.notification') IS NOT NULL AND
		 (SELECT column_name FROM information_schema.columns WHERE table_name='notification' AND column_name='user_id') IS NOT NULL THEN
		ALTER TABLE notification ALTER COLUMN user_id TYPE varchar(128) USING user_id::varchar;
		ALTER TABLE notification ALTER COLUMN user_id SET NOT NULL;
	END IF;
END$$;

-- Add/adjust title column (migrate data from subject if present)
DO $$
BEGIN
	IF to_regclass('public.notification') IS NOT NULL THEN
		IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='notification' AND column_name='title') THEN
			ALTER TABLE notification ADD COLUMN title varchar(200);
		END IF;

		-- If subject column exists, copy to title then drop subject
		IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='notification' AND column_name='subject') THEN
			EXECUTE 'UPDATE notification SET title = subject WHERE title IS NULL AND subject IS NOT NULL';
			ALTER TABLE notification DROP COLUMN IF EXISTS subject;
		END IF;

		-- Ensure title is not null (set placeholder if empty)
		UPDATE notification SET title = '(no title)' WHERE title IS NULL;
		ALTER TABLE notification ALTER COLUMN title SET NOT NULL;
	END IF;
END$$;

-- Add new columns if they don't exist
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS priority varchar(20) DEFAULT 'MEDIUM';
ALTER TABLE IF EXISTS notification ALTER COLUMN priority SET NOT NULL;

ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS delivery_channels jsonb;
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS metadata jsonb;
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS sent_at timestamp;

ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS is_read boolean DEFAULT false;
ALTER TABLE IF EXISTS notification ALTER COLUMN is_read SET DEFAULT false;

ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS related_entity_type varchar(50);
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS related_entity_id varchar(100);
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS action_url varchar(500);
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS read_at timestamp;
ALTER TABLE IF EXISTS notification ADD COLUMN IF NOT EXISTS updated_at timestamp DEFAULT now();

-- Ensure message column is not null (V1 had message NOT NULL already) and keep type column
ALTER TABLE IF EXISTS notification ALTER COLUMN message SET NOT NULL;
ALTER TABLE IF EXISTS notification ALTER COLUMN type SET NOT NULL;

-- Ensure created_at has default now()
ALTER TABLE IF EXISTS notification ALTER COLUMN created_at SET DEFAULT now();

-- Create indexes required by the new schema
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_userid_is_read ON notification(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notification(created_at DESC);

-- Create notification_template table
CREATE TABLE IF NOT EXISTS notification_template (
	template_id serial PRIMARY KEY,
	template_code varchar(50) UNIQUE NOT NULL,
	event_type varchar(50) NOT NULL,
	title_template text NOT NULL,
	message_template text NOT NULL,
	supported_channels json,
	is_active boolean DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_notification_template_code ON notification_template(template_code);
CREATE INDEX IF NOT EXISTS idx_notification_template_event_type ON notification_template(event_type);

COMMIT;

