-- Insert bookings for November 7, 2025 (today)
-- This will make data visible immediately when the page loads

-- First, create time slots for November 7, 2025
INSERT INTO time_slots (service_id, slot_date, start_time, end_time, is_available, created_at, updated_at)
VALUES 
(1, '2025-11-07', '09:00:00', '09:30:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-11-07', '10:00:00', '10:45:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '2025-11-07', '11:00:00', '12:00:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '2025-11-07', '14:00:00', '14:30:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '2025-11-07', '15:00:00', '16:30:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Now insert bookings for November 7, 2025
-- Booking 1: Oil Change at 9:00 AM
INSERT INTO bookings (
    service_id, 
    time_slot_id, 
    user_id, 
    customer_name, 
    customer_email, 
    customer_phone, 
    status, 
    assigned_employee_id, 
    notes,
    booking_date,
    created_at, 
    updated_at
)
VALUES 
(
    1,
    (SELECT id FROM time_slots WHERE slot_date = '2025-11-07' AND start_time = '09:00:00' AND service_id = 1 LIMIT 1),
    'cust_nov7_01',
    'Rajesh Kumar',
    'rajesh.kumar@email.com',
    '+94-77-234-5678',
    'CONFIRMED',
    2,
    'Customer requested synthetic oil',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 2: Tire Rotation at 10:00 AM
INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, assigned_employee_id, notes, booking_date, created_at, updated_at)
VALUES (
    2,
    (SELECT id FROM time_slots WHERE slot_date = '2025-11-07' AND start_time = '10:00:00' AND service_id = 2 LIMIT 1),
    'cust_nov7_02',
    'Kavita Nair',
    'kavita.nair@email.com',
    '+94-77-345-6789',
    'CONFIRMED',
    3,
    'All four tires need rotation',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 3: Brake Inspection at 11:00 AM
INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, assigned_employee_id, notes, booking_date, created_at, updated_at)
VALUES (
    3,
    (SELECT id FROM time_slots WHERE slot_date = '2025-11-07' AND start_time = '11:00:00' AND service_id = 3 LIMIT 1),
    'cust_nov7_03',
    'Vikram Singh',
    'vikram.singh@email.com',
    '+94-77-456-7890',
    'PENDING',
    NULL,
    'Customer reports squeaking noise',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 4: Battery Check at 2:00 PM
INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, assigned_employee_id, notes, booking_date, created_at, updated_at)
VALUES (
    4,
    (SELECT id FROM time_slots WHERE slot_date = '2025-11-07' AND start_time = '14:00:00' AND service_id = 4 LIMIT 1),
    'cust_nov7_04',
    'Anjali Reddy',
    'anjali.reddy@email.com',
    '+94-77-567-8901',
    'CONFIRMED',
    5,
    'Battery is 3 years old, needs testing',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Booking 5: General Inspection at 3:00 PM
INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, assigned_employee_id, notes, booking_date, created_at, updated_at)
VALUES (
    5,
    (SELECT id FROM time_slots WHERE slot_date = '2025-11-07' AND start_time = '15:00:00' AND service_id = 5 LIMIT 1),
    'cust_nov7_05',
    'Deepak Sharma',
    'deepak.sharma@email.com',
    '+94-77-678-9012',
    'CONFIRMED',
    2,
    'Pre-purchase inspection for used car',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
