-- ========================================
-- Payment Service - Review Mock Data
-- ========================================
-- Version: V4
-- Description: Inserts sample reviews for testing

-- Review 1: John Doe - 5 stars - PUBLISHED
INSERT INTO customer_reviews (id, bill_id, customer_email, customer_name, service_name, rating, review_text, status, submitted_date, published_date, created_at, updated_at)
SELECT 
    '850e8400-e29b-41d4-a716-446655440001'::UUID,
    '750e8400-e29b-41d4-a716-446655440002'::UUID,
    'emily@example.com',
    'Emily Brown',
    'Oil Change',
    5,
    'Excellent service! Very professional and quick.',
    'PUBLISHED',
    CURRENT_DATE - 1,
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE EXISTS (SELECT 1 FROM customer_bills WHERE id = '750e8400-e29b-41d4-a716-446655440002'::UUID);

-- Review 2: Jane Smith - 4 stars - PENDING (waiting for admin approval)
INSERT INTO customer_reviews (id, bill_id, customer_email, customer_name, service_name, rating, review_text, status, submitted_date, created_at, updated_at)
SELECT 
    '850e8400-e29b-41d4-a716-446655440002'::UUID,
    '750e8400-e29b-41d4-a716-446655440001'::UUID,
    'jane@example.com',
    'Jane Smith',
    'Brake Inspection',
    4,
    'Good service, but had to wait a bit longer than expected.',
    'PENDING',
    CURRENT_DATE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE EXISTS (SELECT 1 FROM customer_bills WHERE id = '750e8400-e29b-41d4-a716-446655440001'::UUID);
