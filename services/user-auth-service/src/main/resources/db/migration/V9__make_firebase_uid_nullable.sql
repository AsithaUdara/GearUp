-- Make firebase_uid column nullable to support employees created by admin
-- Employees will set their Firebase UID later when they complete password setup using OTP

ALTER TABLE users ALTER COLUMN firebase_uid DROP NOT NULL;
