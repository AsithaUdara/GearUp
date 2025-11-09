-- ========================================
-- Appointment Service - Sample Time Slots
-- ========================================
-- Migration: V2__insert_sample_timeslots.sql
-- Description: Creates sample time slots for the next 30 days

-- Function to generate time slots for a service on a specific date
-- This creates hourly slots from 9 AM to 5 PM for each service
DO $$
DECLARE
    service_record RECORD;
    slot_date DATE;
    end_date DATE;
    slot_time TIME;
    end_slot_time TIME;
BEGIN
    -- Set date range (next 30 days)
    slot_date := CURRENT_DATE + INTERVAL '1 day';
    end_date := slot_date + INTERVAL '30 days';
    
    -- Loop through each active service
    FOR service_record IN SELECT id, duration_minutes FROM services WHERE is_active = true
    LOOP
        -- Loop through each date
        WHILE slot_date <= end_date LOOP
            -- Skip weekends (Saturday = 6, Sunday = 0)
            IF EXTRACT(DOW FROM slot_date) NOT IN (0, 6) THEN
                -- Create time slots from 9 AM to 5 PM
                slot_time := '09:00:00';
                WHILE slot_time < '17:00:00' LOOP
                    -- Calculate end time based on service duration
                    end_slot_time := slot_time + (service_record.duration_minutes || ' minutes')::INTERVAL;
                    
                    -- Only insert if end time doesn't exceed 17:00
                    IF end_slot_time <= '17:00:00' THEN
                        INSERT INTO time_slots (service_id, slot_date, start_time, end_time, is_available)
                        VALUES (service_record.id, slot_date, slot_time, end_slot_time, true);
                    END IF;
                    
                    -- Move to next hour
                    slot_time := slot_time + INTERVAL '1 hour';
                END LOOP;
            END IF;
            
            -- Move to next date
            slot_date := slot_date + INTERVAL '1 day';
        END LOOP;
        
        -- Reset slot_date for next service
        slot_date := CURRENT_DATE + INTERVAL '1 day';
    END LOOP;
END $$;

-- Create some sample bookings (optional - for testing)
-- INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, notes)
-- SELECT 
--     1, -- Oil Change service
--     ts.id,
--     'user123',
--     'John Doe',
--     'john.doe@example.com',
--     '+1234567890',
--     'CONFIRMED',
--     'Sample booking for testing'
-- FROM time_slots ts
-- WHERE ts.service_id = 1 
--   AND ts.slot_date = CURRENT_DATE + INTERVAL '2 days'
--   AND ts.start_time = '10:00:00'
-- LIMIT 1;