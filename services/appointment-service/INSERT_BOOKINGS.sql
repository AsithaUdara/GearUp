-- Insert realistic bookings for as_appointment_service database
-- Copy and paste this into pgAdmin Query Tool

-- ==========================================
-- BOOKINGS FOR NOVEMBER 10, 2025 (Sunday)
-- ==========================================

-- Booking 1: John Doe - Oil Change (9:00 AM) - Assigned to Priya Shah
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    1, 1, 'cust_001', 'John Doe', 'john.doe@email.com', '+94-77-123-4567',
    'CONFIRMED', 'Regular oil change - Synthetic oil requested', 2, 
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 2: Sarah Williams - Tire Rotation (9:00 AM) - Assigned to Rohan Mehta
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    2, 169, 'cust_002', 'Sarah Williams', 'sarah.w@email.com', '+94-77-234-5678',
    'CONFIRMED', 'All four tires - check pressure', 3,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 3: Mike Johnson - Brake Inspection (9:00 AM) - Assigned to Dev Patel
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    3, 337, 'cust_003', 'Mike Johnson', 'mike.j@email.com', '+94-77-345-6789',
    'CONFIRMED', 'Brake pads making squeaking noise', 5,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 4: Emily Davis - Battery Check (9:00 AM) - PENDING (No employee assigned yet)
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    4, 505, 'cust_004', 'Emily Davis', 'emily.d@email.com', '+94-77-456-7890',
    'PENDING', 'Battery warning light on dashboard', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 5: Robert Brown - General Inspection (10:00 AM) - Assigned to Priya Shah
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    1, 2, 'cust_005', 'Robert Brown', 'robert.b@email.com', '+94-77-567-8901',
    'CONFIRMED', 'Regular maintenance - 5000 km service', 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 6: Lisa Anderson - Tire Rotation (10:00 AM) - Assigned to Rohan Mehta
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    2, 170, 'cust_006', 'Lisa Anderson', 'lisa.a@email.com', '+94-77-678-9012',
    'CONFIRMED', 'Tire rotation and wheel alignment check', 3,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 7: David Martinez - Brake Inspection (10:00 AM) - Assigned to Dev Patel  
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    3, 338, 'cust_007', 'David Martinez', 'david.m@email.com', '+94-77-789-0123',
    'CONFIRMED', 'Follow-up inspection after brake pad replacement', 5,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 8: James Wilson - Battery Check (10:00 AM) - PENDING
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    4, 506, 'cust_008', 'James Wilson', 'james.w@email.com', '+94-77-890-1234',
    'PENDING', 'Battery discharge issue - car wont start', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 9: Maria Garcia - General Inspection (10:00 AM) - Assigned to Priya Shah
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    5, 674, 'cust_009', 'Maria Garcia', 'maria.g@email.com', '+94-77-901-2345',
    'CONFIRMED', 'Pre-purchase inspection - used car', 2,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Booking 10: Kevin Lee - Oil Change (11:00 AM) - CANCELLED
INSERT INTO bookings (
    service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, 
    status, notes, assigned_employee_id, booking_date, created_at, updated_at
) VALUES (
    1, 3, 'cust_010', 'Kevin Lee', 'kevin.l@email.com', '+94-77-012-3456',
    'CANCELLED', 'Customer called to cancel - rescheduled', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ==========================================
-- VERIFY THE INSERTIONS
-- ==========================================

SELECT 
    b.id,
    s.name as service,
    ts.slot_date,
    ts.start_time,
    ts.end_time,
    b.customer_name,
    b.customer_phone,
    b.status,
    e.name as assigned_to,
    b.notes
FROM bookings b
JOIN services s ON b.service_id = s.id
JOIN time_slots ts ON b.time_slot_id = ts.id
LEFT JOIN employees e ON b.assigned_employee_id = e.id
ORDER BY ts.slot_date, ts.start_time, b.customer_name;
