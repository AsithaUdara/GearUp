-- Insert dummy services matching the screenshots
INSERT INTO services (name, description, duration_minutes, price, is_active, created_at, updated_at) VALUES
('Oil Change & Filter', 'Complete oil change with premium filter replacement', 30, 5000.00, true, NOW(), NOW()),
('Full Service', 'Comprehensive vehicle inspection and maintenance', 120, 15000.00, true, NOW(), NOW()),
('Brake Service', 'Brake pad replacement and brake fluid check', 90, 12000.00, true, NOW(), NOW()),
('Engine Diagnostic', 'Complete engine health check and diagnostics', 60, 8000.00, true, NOW(), NOW()),
('Tire Service', 'Tire rotation, balancing, and alignment check', 45, 6000.00, true, NOW(), NOW()),
('AC Service', 'Air conditioning system cleaning and gas refill', 75, 9000.00, true, NOW(), NOW());

-- Insert time slots for the next 14 days
-- Time slots: 06:00, 09:00, 10:00, 11:00, 12:00, 01:00 PM, 02:00 PM, 03:00 PM, 04:00 PM, 05:00 PM, 07:00 PM
INSERT INTO time_slots (service_id, date, start_time, end_time, is_available, created_at, updated_at)
SELECT 
    s.id as service_id,
    CURRENT_DATE + (d.day_offset || ' days')::INTERVAL as date,
    t.start_time,
    t.end_time,
    true as is_available,
    NOW() as created_at,
    NOW() as updated_at
FROM 
    services s,
    (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10), (11), (12), (13), (14)) as d(day_offset),
    (VALUES 
        ('06:00:00', '06:30:00'),
        ('09:00:00', '09:30:00'),
        ('10:00:00', '10:30:00'),
        ('11:00:00', '11:30:00'),
        ('12:00:00', '12:30:00'),
        ('13:00:00', '13:30:00'),
        ('14:00:00', '14:30:00'),
        ('15:00:00', '15:30:00'),
        ('16:00:00', '16:30:00'),
        ('17:00:00', '17:30:00'),
        ('19:00:00', '19:30:00')
    ) as t(start_time, end_time)
WHERE s.is_active = true;

-- Mark some random time slots as unavailable (booked)
UPDATE time_slots 
SET is_available = false 
WHERE id IN (
    SELECT id FROM time_slots 
    WHERE RANDOM() < 0.3 -- 30% of slots will be marked as booked
    LIMIT 100
);
