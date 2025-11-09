-- ========================================
-- Payment Service - Seed Data for Development/Testing
-- ========================================
-- Version: V2
-- Description: Inserts mock data for testing and development

-- ========================================
-- Sample Payment Requests
-- ========================================

-- 1. PENDING: John Doe - Toyota Camry
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'John Doe', 'john@example.com', 'Toyota Camry 2020 - ABC123', 90.00, 'PENDING', 'Mike (Technician)', CURRENT_DATE - 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Oil Change', 50.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Tire Service', 40.00, CURRENT_TIMESTAMP);

-- 2. APPROVED: Jane Smith - Honda Civic (with bill)
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, approved_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440002', 'Jane Smith', 'jane@example.com', 'Honda Civic 2019 - XYZ789', 165.00, 'APPROVED', 'Sarah (Technician)', CURRENT_DATE - 5, CURRENT_DATE - 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 'Brake Inspection', 45.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 'Engine Diagnostic', 120.00, CURRENT_TIMESTAMP);

INSERT INTO customer_bills (id, payment_request_id, customer_email, customer_name, vehicle_info, total_amount, tax_amount, final_amount, approved_date, payment_status, review_submitted, created_at, updated_at)
VALUES 
('750e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'jane@example.com', 'Jane Smith', 'Honda Civic 2019 - XYZ789', 165.00, 16.50, 181.50, CURRENT_DATE - 3, 'UNPAID', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. REJECTED: Bob Wilson - Ford F-150
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, rejected_date, rejection_reason, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440003', 'Bob Wilson', 'bob@example.com', 'Ford F-150 2021 - DEF456', 250.00, 'REJECTED', 'Tom (Technician)', CURRENT_DATE - 4, CURRENT_DATE - 2, 'Incorrect vehicle information provided', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440003', 'Transmission Service', 250.00, CURRENT_TIMESTAMP);

-- 4. PENDING: Alice Johnson - Tesla Model 3
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440004', 'Alice Johnson', 'alice@example.com', 'Tesla Model 3 2023 - TES123', 225.00, 'PENDING', 'Alex (Technician)', CURRENT_DATE - 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440004', 'Battery Check', 75.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440004', 'Software Update', 150.00, CURRENT_TIMESTAMP);

-- 5. PENDING: David Chen - BMW X5
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440005', 'David Chen', 'david@example.com', 'BMW X5 2022 - BMW999', 180.00, 'PENDING', 'Mike (Technician)', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440005', 'Air Filter Replacement', 80.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440005', 'Wheel Alignment', 100.00, CURRENT_TIMESTAMP);

-- 6. APPROVED with PAID bill: Emily Brown - Mazda CX-5
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, approved_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440006', 'Emily Brown', 'emily@example.com', 'Mazda CX-5 2021 - MAZ555', 130.00, 'APPROVED', 'Sarah (Technician)', CURRENT_DATE - 10, CURRENT_DATE - 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440006', 'Coolant Flush', 70.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440006', 'Cabin Air Filter', 60.00, CURRENT_TIMESTAMP);

INSERT INTO customer_bills (id, payment_request_id, customer_email, customer_name, vehicle_info, total_amount, tax_amount, final_amount, approved_date, payment_status, paid_date, review_submitted, created_at, updated_at)
VALUES 
('750e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440006', 'emily@example.com', 'Emily Brown', 'Mazda CX-5 2021 - MAZ555', 130.00, 13.00, 143.00, CURRENT_DATE - 8, 'PAID', CURRENT_DATE - 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 7. PENDING: Michael Scott - Subaru Outback
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440007', 'Michael Scott', 'michael@example.com', 'Subaru Outback 2020 - SUB111', 95.00, 'PENDING', 'Tom (Technician)', CURRENT_DATE - 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440007', 'Wiper Blade Replacement', 35.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440007', 'Light Bulb Replacement', 60.00, CURRENT_TIMESTAMP);

-- 8. PENDING: Sarah Williams - Mercedes-Benz C-Class
INSERT INTO payment_requests (id, customer_name, customer_email, vehicle_info, total_amount, status, submitted_by, submitted_date, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440008', 'Sarah Williams', 'sarah@example.com', 'Mercedes-Benz C-Class 2023 - MER777', 350.00, 'PENDING', 'Alex (Technician)', CURRENT_DATE - 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment_request_services (id, payment_request_id, description, price, created_at)
VALUES 
('650e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440008', 'Premium Oil Change', 120.00, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440008', 'Brake Pad Replacement', 230.00, CURRENT_TIMESTAMP);
