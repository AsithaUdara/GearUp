-- Add OTP fields to users table for password setup flow
ALTER TABLE users ADD COLUMN IF NOT EXISTS setup_otp VARCHAR(6);
ALTER TABLE users ADD COLUMN IF NOT EXISTS setup_otp_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_password_set BOOLEAN DEFAULT FALSE;

-- Update existing users to have is_password_set = true (they already have passwords)
UPDATE users SET is_password_set = TRUE WHERE is_password_set IS NULL OR is_password_set = FALSE;
