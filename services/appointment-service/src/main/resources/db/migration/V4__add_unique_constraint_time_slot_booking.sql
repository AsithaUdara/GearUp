-- Migration V5: Add unique constraint to prevent double-booking
-- This ensures only ONE active (non-cancelled) booking can exist per time slot

-- Step 1: Delete any existing duplicate bookings (keep the oldest one)
DELETE FROM bookings 
WHERE id NOT IN (
    SELECT MIN(id) 
    FROM bookings 
    WHERE status <> 'CANCELLED'
    GROUP BY time_slot_id
)
AND status <> 'CANCELLED';

-- Step 2: Add a partial unique index
-- This allows multiple CANCELLED bookings but only one active booking per slot
CREATE UNIQUE INDEX idx_unique_active_booking_per_slot 
ON bookings (time_slot_id) 
WHERE status <> 'CANCELLED';

-- Step 3: Add check constraint to ensure time slot availability matches booking status
-- When a slot has an active booking, it should be marked as unavailable
-- (This will be enforced by application logic, but we add a comment for clarity)
COMMENT ON INDEX idx_unique_active_booking_per_slot IS 
'Ensures only one active (non-cancelled) booking can exist per time slot. Multiple cancelled bookings are allowed.';
