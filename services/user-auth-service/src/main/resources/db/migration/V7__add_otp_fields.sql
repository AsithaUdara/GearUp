-- Add OTP fields to users table for password setup
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS setup_otp VARCHAR(6),
ADD COLUMN IF NOT EXISTS setup_otp_expires_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS is_password_set BOOLEAN DEFAULT FALSE;

-- Create index for OTP lookups
CREATE INDEX IF NOT EXISTS idx_users_setup_otp ON users(setup_otp) WHERE setup_otp IS NOT NULL;

-- Update existing users to mark password as set (they have Firebase UID)
UPDATE users 
SET is_password_set = TRUE 
WHERE firebase_uid IS NOT NULL AND firebase_uid != '' AND firebase_uid NOT LIKE 'pending_%';
