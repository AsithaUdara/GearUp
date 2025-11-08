-- Update admin@gearup.com Firebase UID to match the logged-in user
UPDATE users 
SET firebase_uid = 'u2sgkfVpdTd9hkrUp5sb3ttiHOt2' 
WHERE email = 'admin@gearup.com';

-- Verify the update
SELECT id, email, firebase_uid, account_status 
FROM users 
WHERE email = 'admin@gearup.com';
