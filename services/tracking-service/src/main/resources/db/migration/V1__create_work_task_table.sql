-- Create work_task table
CREATE TABLE IF NOT EXISTS work_task (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(50) UNIQUE NOT NULL,
    service_id VARCHAR(50) NOT NULL,
    vehicle VARCHAR(100) NOT NULL,
    customer VARCHAR(100) NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    assignee_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'in_progress', 'completed')),
    progress_step INTEGER CHECK (progress_step BETWEEN 1 AND 5),
    notes TEXT,
    time VARCHAR(20),
    estimated_duration INTEGER,
    actual_duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_work_task_assignee ON work_task(assignee_id);
CREATE INDEX idx_work_task_status ON work_task(status);
CREATE INDEX idx_work_task_vehicle ON work_task(vehicle);
CREATE INDEX idx_work_task_service_id ON work_task(service_id);
CREATE INDEX idx_work_task_completed_at ON work_task(completed_at);

-- Add comments for documentation
COMMENT ON TABLE work_task IS 'Employee work tasks and service assignments';
COMMENT ON COLUMN work_task.task_id IS 'Unique task identifier';
COMMENT ON COLUMN work_task.assignee_id IS 'Employee Firebase UID';
COMMENT ON COLUMN work_task.progress_step IS 'Task progress from 1-5';
