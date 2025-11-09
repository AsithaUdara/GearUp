-- Insert sample bookings into as_appointment_service database
-- Run this in pgAdmin connected to as_appointment_service database

-- First, let's check what time_slots we have
-- SELECT * FROM time_slots ORDER BY slot_date, start_time;

-- Insert sample bookings for today and upcoming dates
-- Make sure the time_slot_id and service_id exist in your database

-- Booking 1: John Doe - Oil Change (Confirmed)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    1, -- Oil Change service
    1, -- First time slot (adjust based on your time_slots table)
    'user_001',
    'John Doe',
    'john.doe@email.com',
    '+1-555-0101',
    'CONFIRMED',
    'Regular oil change service',
    1, -- First employee (adjust based on your employees table)
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 2: Jane Smith - Tire Rotation (Confirmed)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    2, -- Tire Rotation service
    2, -- Second time slot
    'user_002',
    'Jane Smith',
    'jane.smith@email.com',
    '+1-555-0102',
    'CONFIRMED',
    'All four tires need rotation',
    1, -- First employee
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 3: Mike Johnson - Brake Inspection (In Progress)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    3, -- Brake Inspection service
    3, -- Third time slot
    'user_003',
    'Mike Johnson',
    'mike.johnson@email.com',
    '+1-555-0103',
    'CONFIRMED',
    'Brake pads making noise',
    2, -- Second employee
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 4: Sarah Williams - Battery Check (Pending)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    booking_date,
    created_at,
    updated_at
) VALUES (
    4, -- Battery Check service
    4, -- Fourth time slot
    'user_004',
    'Sarah Williams',
    'sarah.williams@email.com',
    '+1-555-0104',
    'PENDING',
    'Battery light came on dashboard',
    NULL, -- Not yet assigned
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 5: Robert Brown - General Inspection (Confirmed)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    5, -- General Inspection service
    5, -- Fifth time slot
    'user_005',
    'Robert Brown',
    'robert.brown@email.com',
    '+1-555-0105',
    'CONFIRMED',
    'Annual inspection due',
    3, -- Third employee
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 6: Emily Davis - Oil Change (Completed)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    1, -- Oil Change service
    6, -- Sixth time slot
    'user_006',
    'Emily Davis',
    'emily.davis@email.com',
    '+1-555-0106',
    'COMPLETED',
    'Synthetic oil used',
    1, -- First employee
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP
);

-- Booking 7: David Martinez - Tire Rotation (Cancelled)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    booking_date,
    created_at,
    updated_at
) VALUES (
    2, -- Tire Rotation service
    7, -- Seventh time slot
    'user_007',
    'David Martinez',
    'david.martinez@email.com',
    '+1-555-0107',
    'CANCELLED',
    'Customer called to cancel',
    NULL,
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP
);

-- Booking 8: Lisa Anderson - Brake Inspection (Confirmed for tomorrow)
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    notes,
    assigned_employee_id,
    booking_date,
    created_at,
    updated_at
) VALUES (
    3, -- Brake Inspection service
    8, -- Eighth time slot
    'user_008',
    'Lisa Anderson',
    'lisa.anderson@email.com',
    '+1-555-0108',
    'CONFIRMED',
    'Follow-up inspection after brake pad replacement',
    2, -- Second employee
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify the insertions
SELECT 
    b.id,
    s.name as service_name,
    b.customer_name,
    b.customer_email,
    b.customer_phone,
    b.status,
    b.assigned_employee_id,
    b.notes,
    b.booking_date
FROM bookings b
JOIN services s ON b.service_id = s.id
ORDER BY b.created_at DESC;
