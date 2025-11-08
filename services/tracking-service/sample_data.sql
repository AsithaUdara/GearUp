-- =====================================================
-- Sample Data for GearUp Tracking Service Database
-- =====================================================
-- Run these INSERT statements in PgAdmin Query Tool
-- Database: as_tracking_service (or your tracking database name)
-- =====================================================

-- =====================================================
-- 1. WORK_TASK TABLE - Employee Tasks
-- =====================================================
-- Insert sample work tasks for employee 'emp-1'
-- Status values: 'pending', 'in_progress', 'completed'
-- Progress step: 1-5 (only for in_progress tasks)

INSERT INTO work_task (
    task_id, 
    service_id, 
    vehicle, 
    customer, 
    service_type, 
    assignee_id, 
    status, 
    progress_step, 
    notes, 
    time, 
    estimated_duration, 
    actual_duration,
    created_at,
    updated_at,
    completed_at
) VALUES

(
    'task-1730966400001',
    'SVC-2024-00789',
    'Toyota Camry - 2021',
    'Jane Smith',
    'Brake Pad Replacement',
    'emp-1',
    'in_progress',
    3,
    'Caliper pins inspected, brake pads removed. Installing new pads.',
    '10:35 AM',
    60,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    NULL
),


(
    'task-1730966400002',
    'SVC-2024-00785',
    'Honda Civic - 2019',
    'John Doe',
    'Oil Change',
    'emp-1',
    'completed',
    5,
    'Premium synthetic oil used, filter replaced. Vehicle ready.',
    '10:15 AM',
    20,
    18,
    CURRENT_TIMESTAMP - INTERVAL '4 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),

(
    'task-1730966400003',
    'SVC-2024-00786',
    'Ford F-150 - 2020',
    'Peter Jones',
    'Brake Inspection',
    'emp-1',
    'completed',
    5,
    'Full brake system inspection completed. All components in good condition.',
    '11:30 AM',
    30,
    25,
    CURRENT_TIMESTAMP - INTERVAL '5 hours',
    CURRENT_TIMESTAMP - INTERVAL '4 hours',
    CURRENT_TIMESTAMP - INTERVAL '4 hours'
),


(
    'task-1730966400004',
    'SVC-2024-00787',
    'Toyota Corolla - 2018',
    'Emily Clark',
    'Tire Rotation',
    'emp-1',
    'pending',
    1,
    'Standard tire rotation service requested.',
    '01:45 PM',
    25,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    NULL
),

(
    'task-1730966400005',
    'SVC-2024-00788',
    'BMW 3 Series - 2022',
    'Michael Brown',
    'Engine Diagnostic',
    'emp-1',
    'pending',
    1,
    'Check engine light on. Full diagnostic scan required.',
    '02:30 PM',
    45,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    NULL
),

-- Another In Progress Task
(
    'task-1730966400006',
    'SVC-2024-00790',
    'Mercedes C-Class - 2021',
    'Sarah Wilson',
    'AC System Service',
    'emp-1',
    'in_progress',
    2,
    'AC filter replaced, refrigerant level checked. Testing system.',
    '11:00 AM',
    40,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    NULL
);

-- =====================================================
-- 2. MODIFICATION_REQUEST TABLE - Customer Requests
-- =====================================================
-- Insert sample modification requests
-- Status values: 'pending', 'approved', 'rejected', 'in_progress', 'completed'
-- Type values: 'add_service', 'remove_service', 'change_service', 'urgent_repair'

INSERT INTO modification_request (
    request_id,
    service_id,
    vehicle,
    customer,
    type,
    title,
    description,
    status,
    requested_by,
    assigned_to_employee_id,
    estimated_cost,
    estimated_duration,
    requested_at,
    approved_at,
    completed_at,
    created_at,
    updated_at
) VALUES

(
    'mod-1730966400001',
    'SVC-2024-00789',
    'Toyota Camry - 2021',
    'Jane Smith',
    'add_service',
    'Brake Fluid Replacement',
    'Customer requested brake fluid replacement during brake pad service',
    'pending',
    'customer-jane-smith',
    NULL,
    3500.00,
    20,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'mod-1730966400002',
    'SVC-2024-00791',
    'Honda Accord - 2020',
    'David Lee',
    'urgent_repair',
    'Battery Replacement',
    'Battery completely dead. Vehicle needs immediate attention.',
    'approved',
    'customer-david-lee',
    'emp-1',
    12500.00,
    30,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),

(
    'mod-1730966400003',
    'SVC-2024-00785',
    'Honda Civic - 2019',
    'John Doe',
    'add_service',
    'Air Filter Replacement',
    'Customer requested air filter replacement during oil change',
    'completed',
    'customer-john-doe',
    'emp-1',
    2500.00,
    15,
    CURRENT_TIMESTAMP - INTERVAL '5 hours',
    CURRENT_TIMESTAMP - INTERVAL '4 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '5 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
);

-- =====================================================
-- 3. PARTS_REQUEST TABLE - Parts/Materials Requests
-- =====================================================
-- Insert sample parts requests
-- Status values: 'Pending', 'Approved', 'Rejected'

INSERT INTO parts_request (
    request_id,
    material,
    quantity,
    status,
    date,
    vehicle,
    service_id,
    requested_by,
    notes,
    cost,
    created_at,
    updated_at
) VALUES
(
    'REQ-2024-001',
    'Brake Pads',
    4,
    'Approved',
    CURRENT_DATE,
    'Toyota Camry - 2021',
    'SVC-2024-00789',
    'emp-1',
    'Front brake pads required for brake pad replacement service',
    8500.00,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),

(
    'REQ-2024-002',
    'Engine Oil 5L',
    2,
    'Approved',
    CURRENT_DATE,
    'Honda Civic - 2019',
    'SVC-2024-00785',
    'emp-1',
    'Synthetic engine oil for oil change service',
    12500.00,
    CURRENT_TIMESTAMP - INTERVAL '4 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),

(
    'REQ-2024-003',
    'Brake Fluid DOT 4',
    1,
    'Approved',
    CURRENT_DATE,
    'Toyota Camry - 2021',
    'SVC-2024-00789',
    'emp-1',
    'Brake fluid for brake service',
    3500.00,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
),

(
    'REQ-2024-004',
    'AC Filter',
    1,
    'Pending',
    CURRENT_DATE,
    'Mercedes C-Class - 2021',
    'SVC-2024-00790',
    'emp-1',
    'AC filter for AC system service',
    4500.00,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);

-- =====================================================
-- 4. SERVICE_PROGRESS TABLE - Customer Progress View
-- =====================================================
-- Insert sample service progress for customer view
-- Status values: 'scheduled', 'in_progress', 'completed', 'delayed'

INSERT INTO service_progress (
    progress_id,
    service_id,
    vehicle_model,
    vehicle_year,
    customer_name,
    customer_phone,
    customer_email,
    appointment_date,
    appointment_time,
    estimated_completion,
    current_step,
    total_steps,
    overall_status,
    technician_id,
    technician_name,
    location_name,
    last_update,
    created_at,
    updated_at
) VALUES
(
    'PROG-2024-001',
    'SVC-2024-00789',
    'Toyota Camry',
    '2021',
    'Jane Smith',
    '+94 77 123 4567',
    'jane.smith@email.com',
    CURRENT_DATE,
    '10:00 AM',
    '2:30 PM',
    3,
    6,
    'in_progress',
    'emp-1',
    'Mike Johnson',
    'GearUp Service Center - Colombo',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
),

(
    'PROG-2024-002',
    'SVC-2024-00790',
    'Mercedes C-Class',
    '2021',
    'Sarah Wilson',
    '+94 77 234 5678',
    'sarah.wilson@email.com',
    CURRENT_DATE,
    '11:00 AM',
    '1:00 PM',
    2,
    5,
    'in_progress',
    'emp-1',
    'Mike Johnson',
    'GearUp Service Center - Colombo',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
);

-- =====================================================
-- Verification Queries
-- =====================================================
-- Run these to verify the data was inserted correctly:

-- SELECT COUNT(*) FROM work_task WHERE assignee_id = 'emp-1';
-- SELECT * FROM work_task WHERE assignee_id = 'emp-1' ORDER BY created_at DESC;
-- SELECT * FROM modification_request WHERE status IN ('pending', 'approved');
-- SELECT * FROM parts_request WHERE date = CURRENT_DATE;
-- SELECT * FROM service_progress;

-- =====================================================
-- Notes:
-- =====================================================
-- 1. Make sure you're connected to the correct database (as_tracking_service)
-- 2. The employee ID 'emp-1' matches what's used in the frontend
-- 3. Status values must be lowercase for work_task and modification_request
-- 4. Status values for parts_request use Title Case: 'Pending', 'Approved', 'Rejected'
-- 5. After inserting, refresh your frontend page to see the data
-- 6. You can modify the dates/times to match your current timezone

