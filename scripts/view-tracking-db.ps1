# ========================================
# View Tracking Service Database Data
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tracking Service Database Viewer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbName = "as_tracking_service"
$dbUser = "svc_tracking_service"
$dbPassword = "tracking_svc_pass_2024"
$containerName = "gearup-postgres"

Write-Host "Database: $dbName" -ForegroundColor Yellow
Write-Host "User: $dbUser" -ForegroundColor Yellow
Write-Host ""

# Function to run SQL query
function Invoke-PostgresQuery {
    param (
        [string]$Query,
        [string]$Description
    )
    
    Write-Host "--- $Description ---" -ForegroundColor Cyan
    docker exec -i $containerName psql -U $dbUser -d $dbName -c $Query
    Write-Host ""
}

# Show all tables
Write-Host "1. Listing all tables:" -ForegroundColor Green
Invoke-PostgresQuery -Query "\dt" -Description "Tables in tracking service database"

# Show work_tasks data
Write-Host "2. Work Tasks Data:" -ForegroundColor Green
Invoke-PostgresQuery -Query "SELECT id, task_id, service_id, vehicle, customer, service_type, assignee_id, status, progress_step, created_at FROM work_task ORDER BY created_at DESC LIMIT 10;" -Description "Recent Work Tasks"

# Show modification_requests data
Write-Host "3. Modification Requests Data:" -ForegroundColor Green
Invoke-PostgresQuery -Query "SELECT id, request_id, service_id, vehicle, customer, type, title, status, requested_by, created_at FROM modification_request ORDER BY created_at DESC LIMIT 10;" -Description "Recent Modification Requests"

# Show parts_requests data
Write-Host "4. Parts Requests Data:" -ForegroundColor Green
Invoke-PostgresQuery -Query "SELECT id, request_id, material, quantity, status, vehicle, service_id, requested_by, cost, created_at FROM parts_request ORDER BY created_at DESC LIMIT 10;" -Description "Recent Parts Requests"

# Show service_progress data
Write-Host "5. Service Progress Data:" -ForegroundColor Green
Invoke-PostgresQuery -Query "SELECT id, progress_id, service_id, vehicle_model, customer_name, current_step, total_steps, overall_status, technician_id, last_update FROM service_progress ORDER BY last_update DESC LIMIT 10;" -Description "Service Progress Records"

# Count records
Write-Host "6. Record Counts:" -ForegroundColor Green
Invoke-PostgresQuery -Query "SELECT 'work_task' as table_name, COUNT(*) as count FROM work_task UNION ALL SELECT 'modification_request', COUNT(*) FROM modification_request UNION ALL SELECT 'parts_request', COUNT(*) FROM parts_request UNION ALL SELECT 'service_progress', COUNT(*) FROM service_progress;" -Description "Total Records per Table"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Database View Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Alternative: Use PgAdmin web interface:" -ForegroundColor Yellow
Write-Host "  URL: http://localhost:5050" -ForegroundColor Yellow
Write-Host "  Email: admin@gearup.com" -ForegroundColor Yellow
Write-Host "  Password: admin123" -ForegroundColor Yellow
Write-Host ""
Write-Host "Or connect directly with psql:" -ForegroundColor Yellow
Write-Host "  docker exec -it gearup-postgres psql -U svc_tracking_service -d as_tracking_service" -ForegroundColor Yellow

