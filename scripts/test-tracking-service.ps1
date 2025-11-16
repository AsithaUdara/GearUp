# ========================================
# GearUp Backend - Tracking Service Test (PowerShell)
# ========================================
# This script tests the tracking service endpoints using real database data

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing GearUp Tracking Service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration - Using actual data from database
$BASE_URL = "http://localhost:8091"
$EXISTING_TASK_ID = "task-1762429724199"  # Existing task from DB
$VEHICLE_ID = "TEST-VEHICLE-001"
$SERVICE_ID = "svc-test-001"
$SERVICE_ID_WITH_PROGRESS = "SVC-2024-00789"  # Has progress data
$EMPLOYEE_ID = "emp-001"

# Track test results
$testsPassed = 0
$testsFailed = 0

# Helper function to display test results
function Test-Endpoint {
    param(
        [string]$TestNumber,
        [string]$Description,
        [scriptblock]$TestBlock
    )
    
    Write-Host "`n[$TestNumber] $Description" -ForegroundColor Yellow
    try {
        & $TestBlock
        $script:testsPassed++
        return $true
    }
    catch {
        Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Gray
        }
        $script:testsFailed++
        return $false
    }
}

# ========================================
# TASK MANAGEMENT TESTS
# ========================================
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "TASK MANAGEMENT TESTS" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

# Test 1: Create a new task
$newTaskId = $null
Test-Endpoint "1" "Create New Task (POST /api/tracking/tasks)" {
    $taskData = @{
        vehicle = $VEHICLE_ID
        serviceId = $SERVICE_ID
        customer = "Test Customer"
        serviceType = "Oil Change"
        assigneeId = $EMPLOYEE_ID
        estimatedDuration = 120
        notes = "Automated test task creation"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks" -Method POST -Body $taskData -ContentType "application/json"
    $script:newTaskId = $response.id
    Write-Host "[SUCCESS] Task created successfully" -ForegroundColor Green
    Write-Host "  Task ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Task ID (String): $($response.taskId)" -ForegroundColor Gray
    Write-Host "  Status: $($response.status)" -ForegroundColor Gray
    Write-Host "  Vehicle: $($response.vehicle)" -ForegroundColor Gray
}

# Test 2: Get task by ID (using existing task)
Test-Endpoint "2" "Get Task by ID (GET /api/tracking/tasks/{id})" {
    $taskId = if ($newTaskId) { $newTaskId } else { 1 }
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId" -Method GET
    Write-Host "[SUCCESS] Task retrieved" -ForegroundColor Green
    Write-Host "  Task ID: $($response.taskId)" -ForegroundColor Gray
    Write-Host "  Status: $($response.status)" -ForegroundColor Gray
    Write-Host "  Service Type: $($response.serviceType)" -ForegroundColor Gray
}

# Test 3: Get tasks by vehicle
Test-Endpoint "3" "Get Tasks by Vehicle (GET /api/tracking/tasks/vehicle/{vehicleId})" {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/vehicle/$VEHICLE_ID" -Method GET
    Write-Host "[SUCCESS] Vehicle tasks retrieved" -ForegroundColor Green
    Write-Host "  Total tasks found: $($response.Count)" -ForegroundColor Gray
    if ($response.Count -gt 0) {
        Write-Host "  Latest task: $($response[0].taskId)" -ForegroundColor Gray
        Write-Host "  Service type: $($response[0].serviceType)" -ForegroundColor Gray
    }
}

# Test 4: Get tasks by service ID
Test-Endpoint "4" "Get Tasks by Service ID (GET /api/tracking/tasks/service/{serviceId})" {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/service/$SERVICE_ID" -Method GET
    Write-Host "[SUCCESS] Service tasks retrieved" -ForegroundColor Green
    Write-Host "  Total tasks found: $($response.Count)" -ForegroundColor Gray
}

# Test 5: Update task status (use lowercase enum values)
Test-Endpoint "5" "Update Task Status (PUT /api/tracking/tasks/{id})" {
    $taskId = if ($newTaskId) { $newTaskId } else { 1 }
    $updateData = @{
        status = "in_progress"  # Note: lowercase as per DB enum
        notes = "Task in progress - automated test"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId" -Method PUT -Body $updateData -ContentType "application/json"
    Write-Host "[SUCCESS] Task status updated" -ForegroundColor Green
    Write-Host "  New status: $($response.status)" -ForegroundColor Gray
}

# ========================================
# TIME LOGGING TESTS
# ========================================
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "TIME LOGGING TESTS" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

# Test 6: Start time log
$timeLogStarted = $false
Test-Endpoint "6" "Start Time Log (POST /api/tracking/tasks/{id}/time-logs/start)" {
    $taskId = if ($newTaskId) { $newTaskId } else { 1 }
    $startData = @{
        notes = "Starting work - automated test"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId/time-logs/start?employeeId=$EMPLOYEE_ID" -Method POST -Body $startData -ContentType "application/json"
    $script:timeLogStarted = $true
    Write-Host "[SUCCESS] Time log started" -ForegroundColor Green
    Write-Host "  Log ID: $($response.logId)" -ForegroundColor Gray
    Write-Host "  Start time: $($response.startTime)" -ForegroundColor Gray
}

# Test 7: Get active time log
if ($timeLogStarted) {
    Start-Sleep -Seconds 2
    Test-Endpoint "7" "Get Active Time Log (GET /api/tracking/tasks/{id}/time-logs/active)" {
        $taskId = if ($newTaskId) { $newTaskId } else { 1 }
        $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId/time-logs/active?employeeId=$EMPLOYEE_ID" -Method GET
        Write-Host "[SUCCESS] Active time log retrieved" -ForegroundColor Green
        Write-Host "  Duration: $($response.durationMinutes) minutes" -ForegroundColor Gray
    }
}

# Test 8: Get all time logs for a task
Test-Endpoint "8" "Get Task Time Logs (GET /api/tracking/tasks/{id}/time-logs)" {
    $taskId = if ($newTaskId) { $newTaskId } else { 1 }
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId/time-logs" -Method GET
    Write-Host "[SUCCESS] Task time logs retrieved" -ForegroundColor Green
    Write-Host "  Total logs: $($response.Count)" -ForegroundColor Gray
}

# Test 9: Stop time log
if ($timeLogStarted) {
    Start-Sleep -Seconds 1
    Test-Endpoint "9" "Stop Time Log (POST /api/tracking/tasks/{id}/time-logs/stop)" {
        $taskId = if ($newTaskId) { $newTaskId } else { 1 }
        $stopData = @{
            notes = "Work completed - automated test"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/tasks/$taskId/time-logs/stop?employeeId=$EMPLOYEE_ID" -Method POST -Body $stopData -ContentType "application/json"
        Write-Host "[SUCCESS] Time log stopped" -ForegroundColor Green
        Write-Host "  Duration: $($response.durationMinutes) minutes" -ForegroundColor Gray
        Write-Host "  Total hours: $($response.durationHours) hours" -ForegroundColor Gray
    }
}

# Test 10: Get employee time logs
Test-Endpoint "10" "Get Employee Time Logs (GET /api/tracking/employees/{id}/time-logs)" {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/employees/$EMPLOYEE_ID/time-logs" -Method GET
    Write-Host "[SUCCESS] Employee time logs retrieved" -ForegroundColor Green
    Write-Host "  Total logs: $($response.Count)" -ForegroundColor Gray
    if ($response.Count -gt 0) {
        $totalHours = ($response | Measure-Object -Property durationHours -Sum).Sum
        Write-Host "  Total hours logged: $([math]::Round($totalHours, 2)) hours" -ForegroundColor Gray
    }
}

# ========================================
# SERVICE PROGRESS TESTS
# ========================================
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "SERVICE PROGRESS TESTS" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

# Test 11: Get service progress (using service ID that has progress data)
Test-Endpoint "11" "Get Service Progress (GET /api/tracking/progress/service/{serviceId})" {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/progress/service/$SERVICE_ID_WITH_PROGRESS" -Method GET
    Write-Host "[SUCCESS] Service progress retrieved" -ForegroundColor Green
    Write-Host "  Service ID: $($response.serviceId)" -ForegroundColor Gray
    Write-Host "  Current step: $($response.currentStep) / $($response.totalSteps)" -ForegroundColor Gray
    Write-Host "  Progress: $($response.progressPercentage)%" -ForegroundColor Gray
    Write-Host "  Status: $($response.overallStatus)" -ForegroundColor Gray
    Write-Host "  Customer: $($response.customerName)" -ForegroundColor Gray
}

# Test 12: Update service progress
Test-Endpoint "12" "Update Service Progress (PUT /api/tracking/progress/service/{serviceId})" {
    $newStep = 4
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/tracking/progress/service/$SERVICE_ID_WITH_PROGRESS`?currentStep=$newStep" -Method PUT
    Write-Host "[SUCCESS] Service progress updated" -ForegroundColor Green
    Write-Host "  New step: $($response.currentStep)" -ForegroundColor Gray
    Write-Host "  New progress: $($response.progressPercentage)%" -ForegroundColor Gray
}

# ========================================
# TEST SUMMARY
# ========================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tests Passed: " -NoNewline
Write-Host $testsPassed -ForegroundColor Green
Write-Host "Tests Failed: " -NoNewline
if ($testsFailed -eq 0) {
    Write-Host $testsFailed -ForegroundColor Green
} else {
    Write-Host $testsFailed -ForegroundColor Red
}
Write-Host "Total Tests: $($testsPassed + $testsFailed)" -ForegroundColor Cyan

if ($newTaskId) {
    Write-Host "`nCreated Task ID: $newTaskId" -ForegroundColor Yellow
    Write-Host "  You can use this ID for manual testing" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Service Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check service health
Write-Host "`nTracking Service Health: " -NoNewline
try {
    $healthResponse = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 2
    if ($healthResponse.status -eq "UP") {
        Write-Host "UP" -ForegroundColor Green
    } else {
        Write-Host $healthResponse.status -ForegroundColor Yellow
    }
}
catch {
    Write-Host "DOWN" -ForegroundColor Red
}

# Check database
Write-Host "Database Connection: " -NoNewline
try {
    $healthResponse = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 2
    if ($healthResponse.components.db.status -eq "UP") {
        Write-Host "UP" -ForegroundColor Green
    } else {
        Write-Host $healthResponse.components.db.status -ForegroundColor Yellow
    }
}
catch {
    Write-Host "Unable to check" -ForegroundColor Yellow
}

Write-Host "`nTest completed!" -ForegroundColor Cyan
Write-Host ""
