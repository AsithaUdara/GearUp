-- Sample Service
INSERT INTO services (name, description, duration_minutes, price, is_active)
VALUES ('Oil Change', 'Standard oil change service', 30, 29.99, true);

-- Sample TimeSlot (assuming service id 1)
INSERT INTO time_slots (service_id, slot_date, start_time, end_time, is_available)
VALUES (1, '2025-11-08', '09:00', '09:30', true);

-- Sample Booking (assuming service id 1, time_slot id 1)
INSERT INTO bookings (service_id, time_slot_id, user_id, customer_name, customer_email, customer_phone, status, notes, booking_date)
VALUES (1, 1, 'user123', 'John Doe', 'john@example.com', '1234567890', 'CONFIRMED', 'First booking', '2025-11-08T09:00:00');
