-- Add the assigned_employee_id column to bookings table
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS assigned_employee_id BIGINT;

-- Optionally, add a comment for documentation
COMMENT ON COLUMN bookings.assigned_employee_id IS 'ID of the employee assigned to this booking';