-- Create vehicle_bookings table
CREATE TABLE IF NOT EXISTS vehicle_bookings (
    id VARCHAR(255) PRIMARY KEY,
    vehicle_id VARCHAR(255) NOT NULL,
    vehicle_name VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    booking_start_date TIMESTAMP NOT NULL,
    booking_end_date TIMESTAMP NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_vehicle_bookings_user_id ON vehicle_bookings(user_id);
CREATE INDEX idx_vehicle_bookings_vehicle_id ON vehicle_bookings(vehicle_id);
CREATE INDEX idx_vehicle_bookings_status ON vehicle_bookings(status);
CREATE INDEX idx_vehicle_bookings_created_at ON vehicle_bookings(created_at);
