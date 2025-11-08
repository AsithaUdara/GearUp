-- Create time_log table for tracking employee work time
CREATE TABLE IF NOT EXISTS time_log (
    id BIGSERIAL PRIMARY KEY,
    log_id VARCHAR(50) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL REFERENCES work_task(id) ON DELETE CASCADE,
    employee_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('active', 'completed', 'cancelled')),
    start_time TIMESTAMP NOT NULL,
    stop_time TIMESTAMP,
    duration_minutes INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_time_log_task ON time_log(task_id);
CREATE INDEX idx_time_log_employee ON time_log(employee_id);
CREATE INDEX idx_time_log_status ON time_log(status);
CREATE INDEX idx_time_log_start_time ON time_log(start_time);

-- Add comments for documentation
COMMENT ON TABLE time_log IS 'Employee time log entries for tracking work time on tasks';
COMMENT ON COLUMN time_log.log_id IS 'Unique time log identifier';
COMMENT ON COLUMN time_log.task_id IS 'Reference to work_task';
COMMENT ON COLUMN time_log.employee_id IS 'Employee Firebase UID';
COMMENT ON COLUMN time_log.status IS 'Time log status: active (running), completed (stopped), cancelled';
COMMENT ON COLUMN time_log.duration_minutes IS 'Calculated duration in minutes when stopped';






