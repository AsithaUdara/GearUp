-- V20251120__import_user_auth_data.sql
-- Import user-auth master data and sample rows from dev environment
-- Idempotent: uses ON CONFLICT upserts and DO NOTHING where appropriate.
-- Preserves original IDs and sets sequences to current max after insert.
-- Review and run in a staging environment first.

BEGIN;

-- 1) Roles (master)
INSERT INTO public.roles (id, name, description, is_active, created_at, updated_at)
VALUES
  (1, 'ADMIN', 'System administrator with full access', true, '2025-11-16 11:08:50.21592', '2025-11-16 11:08:50.21592'),
  (2, 'CUSTOMER', 'Regular customer using the platform', true, '2025-11-16 11:08:50.21592', '2025-11-16 11:08:50.21592'),
  (3, 'MECHANIC', 'Service provider/mechanic', true, '2025-11-16 11:08:50.21592', '2025-11-16 11:08:50.21592'),
  (4, 'FLEET_MANAGER', 'Manages fleet of vehicles', true, '2025-11-16 11:08:50.21592', '2025-11-16 11:08:50.21592'),
  (5, 'SUPPORT', 'Customer support representative', true, '2025-11-16 11:08:50.21592', '2025-11-16 11:08:50.21592'),
  (6, 'EMPLOYEE', 'Employee with standard operational access', true, '2025-11-16 11:08:50.969978', '2025-11-16 11:08:50.969978')
ON CONFLICT (name) DO UPDATE
  SET description = EXCLUDED.description,
      is_active = EXCLUDED.is_active,
      updated_at = EXCLUDED.updated_at;

-- 2) Permissions (master)
INSERT INTO public.permissions (id, name, resource, action, description, created_at)
VALUES
  (1, 'user:read', 'USER', 'READ', 'View user information', '2025-11-16 11:08:50.21592'),
  (2, 'user:create', 'USER', 'CREATE', 'Create new users', '2025-11-16 11:08:50.21592'),
  (3, 'user:update', 'USER', 'UPDATE', 'Update user information', '2025-11-16 11:08:50.21592'),
  (4, 'user:delete', 'USER', 'DELETE', 'Delete users', '2025-11-16 11:08:50.21592'),
  (5, 'vehicle:read', 'VEHICLE', 'READ', 'View vehicle information', '2025-11-16 11:08:50.21592'),
  (6, 'vehicle:create', 'VEHICLE', 'CREATE', 'Add new vehicles', '2025-11-16 11:08:50.21592'),
  (7, 'vehicle:update', 'VEHICLE', 'UPDATE', 'Update vehicle information', '2025-11-16 11:08:50.21592'),
  (8, 'vehicle:delete', 'VEHICLE', 'DELETE', 'Remove vehicles', '2025-11-16 11:08:50.21592'),
  (9, 'booking:read', 'BOOKING', 'READ', 'View bookings', '2025-11-16 11:08:50.21592'),
  (10, 'booking:create', 'BOOKING', 'CREATE', 'Create bookings', '2025-11-16 11:08:50.21592'),
  (11, 'booking:update', 'BOOKING', 'UPDATE', 'Modify bookings', '2025-11-16 11:08:50.21592'),
  (12, 'booking:delete', 'BOOKING', 'DELETE', 'Cancel bookings', '2025-11-16 11:08:50.21592'),
  (13, 'payment:read', 'PAYMENT', 'READ', 'View payment information', '2025-11-16 11:08:50.21592'),
  (14, 'payment:process', 'PAYMENT', 'PROCESS', 'Process payments', '2025-11-16 11:08:50.21592'),
  (15, 'payment:refund', 'PAYMENT', 'REFUND', 'Issue refunds', '2025-11-16 11:08:50.21592'),
  (16, 'analytics:read', 'ANALYTICS', 'READ', 'View analytics and reports', '2025-11-16 11:08:50.21592'),
  (17, 'analytics:export', 'ANALYTICS', 'EXPORT', 'Export analytics data', '2025-11-16 11:08:50.21592'),
  (18, 'system:configure', 'SYSTEM', 'CONFIGURE', 'Configure system settings', '2025-11-16 11:08:50.21592'),
  (19, 'system:audit', 'SYSTEM', 'AUDIT', 'View audit logs', '2025-11-16 11:08:50.21592')
ON CONFLICT (name) DO UPDATE
  SET resource = EXCLUDED.resource,
      action = EXCLUDED.action,
      description = EXCLUDED.description,
      created_at = EXCLUDED.created_at;

-- 3) role_permissions (associations)
-- Provided as (role_id, permission_id) pairs from source
INSERT INTO public.role_permissions (role_id, permission_id)
VALUES
  (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),
  (2,5),(2,9),(2,10),(2,11),(2,13),(2,14),
  (3,5),(3,7),(3,9),(3,11),
  (4,5),(4,6),(4,7),(4,9),(4,16),
  (5,1),(5,5),(5,9),(5,11),(5,13),
  (6,5),(6,9),(6,11)
ON CONFLICT DO NOTHING;

-- 4) Users (preserve IDs, upsert on email)
-- Source rows: (id inferred from DB: 1..3)
INSERT INTO public.users (
  id, firebase_uid, email, phone_number, first_name, last_name,
  display_name, profile_image_url, email_verified, phone_verified,
  account_status, created_at, updated_at, last_login_at,
  created_by, updated_by, setup_otp, setup_otp_expires_at, is_password_set
)
VALUES
  (1, 'u2sgkfVpdTd9hkrUp5sb3ttiHOt2', 'admin@gearup.com', NULL, 'Admin', 'User', 'Admin User', NULL, true, false, 'ACTIVE', '2025-11-16 11:15:09.605799', '2025-11-16 11:15:09.605799', NULL, NULL, NULL, NULL, NULL, false),
  (2, 'cml8ZOSPqfUjvaUm7P0tvLeH5x83', 'emp1@gmail.com', NULL, 'Employee01', '', 'Employee01', NULL, true, false, 'ACTIVE', '2025-11-16 11:20:10.717959', '2025-11-16 11:21:40.102076', '2025-11-16 11:21:39.138214', 'u2sgkfVpdTd9hkrUp5sb3ttiHOt2', 'u2sgkfVpdTd9hkrUp5sb3ttiHOt2', NULL, NULL, true),
  (3, 'JGmWcPoDzvTK6tYLXpNhwadcYNl2', 'yow@gmail.com', NULL, 'Yow', '', 'Yow', NULL, false, false, 'ACTIVE', '2025-11-16 18:20:43.455672', '2025-11-16 18:20:43.4557', NULL, NULL, NULL, NULL, NULL, false)
ON CONFLICT (email) DO UPDATE
    SET firebase_uid = EXCLUDED.firebase_uid,
      phone_number = EXCLUDED.phone_number,
      first_name = EXCLUDED.first_name,
      last_name = COALESCE(EXCLUDED.last_name, users.last_name),
      display_name = EXCLUDED.display_name,
      profile_image_url = EXCLUDED.profile_image_url,
      email_verified = EXCLUDED.email_verified,
      phone_verified = EXCLUDED.phone_verified,
      account_status = EXCLUDED.account_status,
      updated_at = EXCLUDED.updated_at,
      last_login_at = EXCLUDED.last_login_at,
      updated_by = COALESCE(EXCLUDED.updated_by, users.updated_by),
      setup_otp = EXCLUDED.setup_otp,
      setup_otp_expires_at = EXCLUDED.setup_otp_expires_at,
      is_password_set = EXCLUDED.is_password_set;

-- 5) user_roles (associations)
-- Source pairs: (user_id, role_id)
INSERT INTO public.user_roles (user_id, role_id)
VALUES
  (1,1),
  (2,6),
  (3,2)
ON CONFLICT DO NOTHING;

-- 6) user_sessions
-- Insert session rows (explicit columns used). If session_token already exists, update expiry/is_active.
INSERT INTO public.user_sessions (
  id, user_id, session_token, refresh_token, device_info, ip_address, user_agent,
  expires_at, refresh_expires_at, created_at, last_accessed_at, is_active
)
VALUES
  (1,
   2,
   'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjbWw4Wk9TUHFmVWp2YVVtN1AwdHZMZUg1eDgzIiwiZmlyZWJhc2VVaWQiOiJjbWw4Wk9TUHFmVWp2YVVtN1AwdHZMZUg1eDgzIiwicm9sZXMiOlsiRU1QTE9ZRUUiXSwidXNlcklkIjoyLCJlbWFpbCI6ImVtcDFAZ21haWwuY29tIiwiaWF0IjoxNzYzMjkyMDk5LCJleHAiOjE3NjMyOTU2OTl9.MdpLyPsiZn2Z-Mh5jZKEFBbL-cwY1gReZEjnvzFa0noxQpOxrr8cJP5RFHcqJJhTdtIr9Nj_xM1WJBuJXdZfAg',
   '7dd4ca63-6053-4f47-98aa-86fa6f8b2174',
   'Web',
   '127.0.0.1',
   NULL,
   '2025-12-16 11:21:40.041153',
   '2025-11-16 11:21:40.041153',
   '2025-11-16 11:21:40.05033',
   '2025-11-16 11:21:40.050368',
   true
  )
ON CONFLICT ON CONSTRAINT user_sessions_session_token_key DO UPDATE
  SET refresh_token = EXCLUDED.refresh_token,
      expires_at = EXCLUDED.expires_at,
      refresh_expires_at = EXCLUDED.refresh_expires_at,
      last_accessed_at = EXCLUDED.last_accessed_at,
      is_active = EXCLUDED.is_active;

-- 7) user_audit_log (history)
INSERT INTO public.user_audit_log (
  id, user_id, action, description, entity_type, entity_id,
  old_values, new_values, ip_address, user_agent, created_at
)
VALUES
  (1, 2, 'USER_NAME_UPDATED', 'User name updated by admin: admin@gearup.com', NULL, NULL,
   '{"id": 2, "email": "emp1@gmail.com", "displayName": "Employee1", "phoneNumber": null, "accountStatus": "ACTIVE"}'::jsonb,
   '{"id": 2, "email": "emp1@gmail.com", "displayName": null, "phoneNumber": null, "accountStatus": "ACTIVE"}'::jsonb,
   NULL, NULL, '2025-11-16 11:20:45.29266'),
  (2, 2, 'USER_UPDATED', 'User updated by admin: admin@gearup.com', NULL, NULL,
   '{"id": 2, "email": "emp1@gmail.com", "displayName": null, "phoneNumber": null, "accountStatus": "ACTIVE"}'::jsonb,
   '{"id": 2, "email": "emp1@gmail.com", "displayName": null, "phoneNumber": null, "accountStatus": "ACTIVE"}'::jsonb,
   NULL, NULL, '2025-11-16 11:20:45.751901'),
  (3, 2, 'PASSWORD_SETUP', 'Password set up successfully for employee/admin account', NULL, NULL,
   NULL, NULL, NULL, NULL, '2025-11-16 11:21:40.090899'),
  (4, 3, 'REGISTER', 'User registered', NULL, NULL,
   '{"id": 3, "email": "yow@gmail.com", "displayName": "Yow", "phoneNumber": null, "accountStatus": "ACTIVE"}'::jsonb,
   NULL, NULL, NULL, '2025-11-16 18:20:44.053195')
ON CONFLICT DO NOTHING;

-- 8) Final: set serial sequences to current max(id) to keep nextval correct
SELECT setval(pg_get_serial_sequence('public.roles','id'), COALESCE((SELECT MAX(id) FROM public.roles), 0));
SELECT setval(pg_get_serial_sequence('public.permissions','id'), COALESCE((SELECT MAX(id) FROM public.permissions), 0));
SELECT setval(pg_get_serial_sequence('public.user_audit_log','id'), COALESCE((SELECT MAX(id) FROM public.user_audit_log), 0));
SELECT setval(pg_get_serial_sequence('public.user_sessions','id'), COALESCE((SELECT MAX(id) FROM public.user_sessions), 0));
SELECT setval(pg_get_serial_sequence('public.users','id'), COALESCE((SELECT MAX(id) FROM public.users), 0));

COMMIT;
