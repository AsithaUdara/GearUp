# GearUp Backend - Test All Database Connections
# Tests connectivity to all service databases

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp All Services Database Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Database configurations for all services
$databases = @(
    @{Name="Automobile Service"; DB="as_automobile_service"},
    @{Name="Notification Service"; DB="as_notification_service"},
    @{Name="User Auth Service"; DB="as_user_auth_service"},
    @{Name="Tracking Service"; DB="as_tracking_service"},
    @{Name="Template Service"; DB="as_template_service"},
    @{Name="Vehicle Service"; DB="as_vehicle_service"},
    @{Name="Customer Service"; DB="as_customer_service"},
    @{Name="Payment Service"; DB="as_payment_service"},
    @{Name="Parts Service"; DB="as_parts_service"},
    @{Name="Analytical Service"; DB="as_analytical_service"},
    @{Name="Chatbot Service"; DB="as_chatbot_service"}
)

$passCount = 0
$failCount = 0
$results = @()

Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

foreach ($db in $databases) {
    Write-Host "Testing $($db.Name)..." -ForegroundColor Yellow
    
    # Test 1: Check if database exists and is accessible
    $connectionTest = docker exec gearup-postgres psql -U postgres -d $($db.DB) -c "SELECT current_database(), current_user;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Database connection: SUCCESS" -ForegroundColor Green
        
        # Test 2: Check if tables exist
        $tableCheck = docker exec gearup-postgres psql -U postgres -d $($db.DB) -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            $tableCount = ($tableCheck | Select-Object -First 1).Trim()
            if ($tableCount -match '^\d+$' -and [int]$tableCount -gt 0) {
                Write-Host "   Tables found: $tableCount" -ForegroundColor Green
                
                # List table names
                $tables = docker exec gearup-postgres psql -U postgres -d $($db.DB) -t -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name;" 2>&1
                $tableList = $tables -replace "`n", ", " -replace "`r", ""
                Write-Host "   Tables: $tableList" -ForegroundColor Gray
                
                $results += @{Service=$db.Name; Status="OK"; Tables=$tableCount; Issue="None"}
                $passCount++
            } else {
                Write-Host "   WARNING: Database exists but NO TABLES found!" -ForegroundColor Yellow
                Write-Host "   This means Flyway migrations have not run yet." -ForegroundColor Yellow
                $results += @{Service=$db.Name; Status="NO TABLES"; Tables=0; Issue="Migrations not executed"}
                $failCount++
            }
        }
    } else {
        Write-Host "   Database connection: FAILED" -ForegroundColor Red
        Write-Host "   Error: $connectionTest" -ForegroundColor Red
        $results += @{Service=$db.Name; Status="FAILED"; Tables=0; Issue="Database not accessible"}
        $failCount++
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Display results table
Write-Host "Service                    Status          Tables  Issue" -ForegroundColor White
Write-Host "-----------------------------------------------------------" -ForegroundColor White
foreach ($result in $results) {
    $statusColor = switch ($result.Status) {
        "OK" { "Green" }
        "NO TABLES" { "Yellow" }
        default { "Red" }
    }
    Write-Host ("{0,-25} {1,-15} {2,-7} {3}" -f $result.Service, $result.Status, $result.Tables, $result.Issue) -ForegroundColor $statusColor
}

Write-Host ""
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Issues: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Yellow" } else { "Green" })

if ($failCount -gt 0) {
    Write-Host ""
    Write-Host "NEXT STEPS:" -ForegroundColor Yellow
    Write-Host "1. For databases with NO TABLES, restart the service to trigger Flyway migrations" -ForegroundColor White
    Write-Host "2. Check service logs: docker logs <service-container-name>" -ForegroundColor White
    Write-Host "3. Verify Flyway is enabled in application.properties" -ForegroundColor White
    Write-Host "4. Run migrations manually if needed (see below)" -ForegroundColor White
    exit 1
} else {
    Write-Host ""
    Write-Host "All databases are healthy!" -ForegroundColor Green
    exit 0
}
