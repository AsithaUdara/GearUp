-- Add EMPLOYEE role and basic permissions if missing
-- This migration ensures OTP flow for EMPLOYEE users works without 404 on role lookup

-- Insert EMPLOYEE role if it does not exist
INSERT INTO roles (name, description)
SELECT 'EMPLOYEE', 'Employee with standard operational access'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'EMPLOYEE');

-- Optionally grant a safe subset of permissions to EMPLOYEE
-- Adjust this list as needed for your product requirements
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'vehicle:read',
    'booking:read',
    'booking:update'
)
WHERE r.name = 'EMPLOYEE'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
