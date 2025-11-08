-- ========================================
-- Payment Service - Customer Reviews Table
-- ========================================
-- Version: V3
-- Description: Creates customer_reviews table for review management

CREATE TABLE customer_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id UUID NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    service_name VARCHAR(500) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_date TIMESTAMP NOT NULL,
    published_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_bill FOREIGN KEY (bill_id) REFERENCES customer_bills(id) ON DELETE CASCADE,
    CONSTRAINT unique_bill_review UNIQUE (bill_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_review_status ON customer_reviews(status);
CREATE INDEX idx_review_customer_email ON customer_reviews(customer_email);
CREATE INDEX idx_review_submitted_date ON customer_reviews(submitted_date DESC);
CREATE INDEX idx_review_rating ON customer_reviews(rating);
