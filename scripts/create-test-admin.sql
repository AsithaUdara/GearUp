-- Create Test Admin User for Testing
-- This script creates a test admin user with a known Firebase UID
-- You can use this to test admin features before setting up real Firebase auth

-- First, ensure roles exist (they should from V2 migration)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN') THEN
        INSERT INTO roles (name, description) VALUES ('ADMIN', 'System administrator with full access');
    END IF;
END $$;

-- Create test admin user
-- Note: In production, Firebase UID should come from Firebase Authentication
-- This is a test user with a mock Firebase UID
INSERT INTO users (
    firebase_uid,
    email,
    display_name,
    first_name,
    last_name,
    phone_number,
    email_verified,
    account_status,
    created_at,
    updated_at
) VALUES (
    'test_admin_uid_12345',  -- Mock Firebase UID (you'll need to create this user in Firebase Console)
    'admin@gearup.com',
    'Test Admin',
    'Test',
    'Admin',
    '+1234567890',
    true,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Assign ADMIN role to the test user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@gearup.com'
  AND r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Create a test employee user
INSERT INTO users (
    firebase_uid,
    email,
    display_name,
    first_name,
    last_name,
    email_verified,
    account_status,
    created_at,
    updated_at
) VALUES (
    'test_employee_uid_67890',
    'employee@gearup.com',
    'Test Employee',
    'Test',
    'Employee',
    true,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Assign EMPLOYEE role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'employee@gearup.com'
  AND r.name = 'EMPLOYEE'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Create a test customer user
INSERT INTO users (
    firebase_uid,
    email,
    display_name,
    first_name,
    last_name,
    email_verified,
    account_status,
    created_at,
    updated_at
) VALUES (
    'test_customer_uid_11111',
    'customer@gearup.com',
    'Test Customer',
    'Test',
    'Customer',
    true,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Assign CUSTOMER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'customer@gearup.com'
  AND r.name = 'CUSTOMER'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Verify users were created
SELECT 
    u.id,
    u.email,
    u.display_name,
    r.name as role,
    u.account_status
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.created_at DESC;
