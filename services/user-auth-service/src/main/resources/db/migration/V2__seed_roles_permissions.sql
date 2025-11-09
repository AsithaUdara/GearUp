-- Insert Default Roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'System administrator with full access'),
('CUSTOMER', 'Regular customer using the platform'),
('MECHANIC', 'Service provider/mechanic'),
('FLEET_MANAGER', 'Manages fleet of vehicles'),
('SUPPORT', 'Customer support representative');

-- Insert Default Permissions

-- User Management Permissions
INSERT INTO permissions (name, resource, action, description) VALUES
('user:read', 'USER', 'READ', 'View user information'),
('user:create', 'USER', 'CREATE', 'Create new users'),
('user:update', 'USER', 'UPDATE', 'Update user information'),
('user:delete', 'USER', 'DELETE', 'Delete users'),

-- Vehicle Management Permissions
('vehicle:read', 'VEHICLE', 'READ', 'View vehicle information'),
('vehicle:create', 'VEHICLE', 'CREATE', 'Add new vehicles'),
('vehicle:update', 'VEHICLE', 'UPDATE', 'Update vehicle information'),
('vehicle:delete', 'VEHICLE', 'DELETE', 'Remove vehicles'),

-- Booking Permissions
('booking:read', 'BOOKING', 'READ', 'View bookings'),
('booking:create', 'BOOKING', 'CREATE', 'Create bookings'),
('booking:update', 'BOOKING', 'UPDATE', 'Modify bookings'),
('booking:delete', 'BOOKING', 'DELETE', 'Cancel bookings'),

-- Payment Permissions
('payment:read', 'PAYMENT', 'READ', 'View payment information'),
('payment:process', 'PAYMENT', 'PROCESS', 'Process payments'),
('payment:refund', 'PAYMENT', 'REFUND', 'Issue refunds'),

-- Analytics Permissions
('analytics:read', 'ANALYTICS', 'READ', 'View analytics and reports'),
('analytics:export', 'ANALYTICS', 'EXPORT', 'Export analytics data'),

-- System Permissions
('system:configure', 'SYSTEM', 'CONFIGURE', 'Configure system settings'),
('system:audit', 'SYSTEM', 'AUDIT', 'View audit logs');

-- Assign Permissions to Roles

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'CUSTOMER'
AND p.name IN (
    'vehicle:read',
    'booking:read',
    'booking:create',
    'booking:update',
    'payment:read',
    'payment:process'
);

-- MECHANIC permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MECHANIC'
AND p.name IN (
    'vehicle:read',
    'vehicle:update',
    'booking:read',
    'booking:update'
);

-- FLEET_MANAGER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'FLEET_MANAGER'
AND p.name IN (
    'vehicle:read',
    'vehicle:create',
    'vehicle:update',
    'booking:read',
    'analytics:read'
);

-- SUPPORT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPPORT'
AND p.name IN (
    'user:read',
    'vehicle:read',
    'booking:read',
    'booking:update',
    'payment:read'
);
