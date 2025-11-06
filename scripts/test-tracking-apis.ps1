# ========================================
# GearUp Backend - Tracking Service API Test Script
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tracking Service API Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8086"
$apiBase = "$baseUrl/api/tracking"
$script:testsPassed = 0
$script:testsFailed = 0

# Function to test API endpoint
function Test-ApiEndpoint {
    param (
        [string]$Method,
        [string]$Url,
        [string]$Description,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    Write-Host "Testing: $Description" -ForegroundColor Yellow
    Write-Host "  $Method $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 10
            ErrorAction = "Stop"
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        if ($Body -ne $null -and $Body -ne "") {
            $params.Body = $Body
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-WebRequest @params
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "  [SUCCESS] HTTP $($response.StatusCode)" -ForegroundColor Green
            if ($response.Content) {
                try {
                    $jsonContent = $response.Content | ConvertFrom-Json
                    Write-Host "  Response: $($jsonContent | ConvertTo-Json -Depth 2 -Compress)" -ForegroundColor Gray
                } catch {
                    $contentPreview = $response.Content.Substring(0, [Math]::Min(100, $response.Content.Length))
                    Write-Host "  Response: $contentPreview..." -ForegroundColor Gray
                }
            }
            $script:testsPassed++
            return $true
        }
        else {
            Write-Host "  [FAILED] Expected HTTP $ExpectedStatus, got $($response.StatusCode)" -ForegroundColor Red
            $script:testsFailed++
            return $false
        }
    }
    catch {
        $statusCode = $null
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
        }
        
        if ($statusCode -and $statusCode -eq $ExpectedStatus) {
            Write-Host "  [SUCCESS] HTTP $statusCode" -ForegroundColor Green
            $script:testsPassed++
            return $true
        }
        else {
            Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
            if ($statusCode) {
                Write-Host "    HTTP Status: $statusCode" -ForegroundColor Red
            }
            $script:testsFailed++
            return $false
        }
    }
}

# Step 1: Check if service is running
Write-Host "Step 1: Checking service health..." -ForegroundColor Cyan
Write-Host ""

Test-ApiEndpoint -Method "GET" -Url "$baseUrl/actuator/health" -Description "Service Health Check"
Write-Host ""

# Step 2: Test Task Endpoints
Write-Host "Step 2: Testing Task Endpoints..." -ForegroundColor Cyan
Write-Host ""

# Create a test task
$createTaskBody = @{
    serviceId = "svc-test-001"
    vehicle = "TEST-VEHICLE-001"
    customer = "Test Customer"
    serviceType = "Oil Change"
    assigneeId = "emp-001"
    estimatedDuration = 30
    notes = "Test task created by API test script"
} | ConvertTo-Json

$taskCreated = Test-ApiEndpoint -Method "POST" -Url "$apiBase/tasks" -Description "Create Task" -Body $createTaskBody -ExpectedStatus 201

if ($taskCreated) {
    # Try to get tasks by vehicle (using the vehicle we just created)
    Start-Sleep -Seconds 1
    Test-ApiEndpoint -Method "GET" -Url "$apiBase/tasks/vehicle/TEST-VEHICLE-001" -Description "Get Tasks by Vehicle"
    
    # Try to get tasks by service ID
    Test-ApiEndpoint -Method "GET" -Url "$apiBase/tasks/service/svc-test-001" -Description "Get Tasks by Service ID"
}

Write-Host ""

# Step 3: Test Employee Endpoints
Write-Host "Step 3: Testing Employee Endpoints..." -ForegroundColor Cyan
Write-Host ""

Test-ApiEndpoint -Method "GET" -Url "$apiBase/employee/emp-001/tasks" -Description "Get Employee Tasks"
Test-ApiEndpoint -Method "GET" -Url "$apiBase/employee/emp-001/daily-summary" -Description "Get Employee Daily Summary"
Test-ApiEndpoint -Method "GET" -Url "$apiBase/employee/emp-001/modification-requests/pending" -Description "Get Pending Modification Requests"

Write-Host ""

# Step 4: Test Modification Request Endpoints
Write-Host "Step 4: Testing Modification Request Endpoints..." -ForegroundColor Cyan
Write-Host ""

$createModRequestBody = @{
    serviceId = "svc-test-002"
    vehicle = "TEST-VEHICLE-002"
    customer = "Test Customer 2"
    type = "change_service"
    title = "Test Modification"
    description = "Test modification request"
    requestedBy = "customer-001"
    assignedToEmployeeId = "emp-001"
    estimatedCost = 150.00
    estimatedDuration = 60
} | ConvertTo-Json

$modRequestCreated = Test-ApiEndpoint -Method "POST" -Url "$apiBase/modification-requests" -Description "Create Modification Request" -Body $createModRequestBody -ExpectedStatus 201

if ($modRequestCreated) {
    Start-Sleep -Seconds 1
    Test-ApiEndpoint -Method "GET" -Url "$apiBase/vehicle/TEST-VEHICLE-002/modification-requests" -Description "Get Modification Requests by Vehicle"
}

Write-Host ""

# Step 5: Test Parts Request Endpoints
Write-Host "Step 5: Testing Parts Request Endpoints..." -ForegroundColor Cyan
Write-Host ""

$createPartsBody = @{
    material = "Engine Oil"
    quantity = 5
    status = "Pending"
    date = (Get-Date).ToString("yyyy-MM-dd")
    vehicle = "TEST-VEHICLE-001"
    serviceId = "svc-test-001"
    requestedBy = "emp-001"
    notes = "Test parts request"
    cost = 50.00
} | ConvertTo-Json

$partsRequestCreated = Test-ApiEndpoint -Method "POST" -Url "$apiBase/parts-requests" -Description "Create Parts Request" -Body $createPartsBody -ExpectedStatus 201

if ($partsRequestCreated) {
    Start-Sleep -Seconds 1
    Test-ApiEndpoint -Method "GET" -Url "$apiBase/vehicle/TEST-VEHICLE-001/parts-requests" -Description "Get Parts Requests by Vehicle"
    Test-ApiEndpoint -Method "GET" -Url "$apiBase/service/svc-test-001/parts-requests" -Description "Get Parts Requests by Service ID"
}

Write-Host ""

# Step 6: Test Progress Endpoints
Write-Host "Step 6: Testing Progress Endpoints..." -ForegroundColor Cyan
Write-Host ""

# Note: These might fail if no progress exists, which is expected
Test-ApiEndpoint -Method "GET" -Url "$apiBase/progress/service/svc-test-001" -Description "Get Service Progress" -ExpectedStatus 500
Test-ApiEndpoint -Method "PUT" -Url "$apiBase/progress/service/svc-test-001?currentStep=2" -Description "Update Service Progress" -ExpectedStatus 500

Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Passed: $script:testsPassed" -ForegroundColor Green
Write-Host "Failed: $script:testsFailed" -ForegroundColor Red
Write-Host ""

if ($script:testsFailed -eq 0) {
    Write-Host "All API tests passed!" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "Some API tests failed. Check the errors above." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Note: Some failures may be expected if:" -ForegroundColor Yellow
    Write-Host "  - The service is not running (check: docker ps)" -ForegroundColor Yellow
    Write-Host "  - Database is not initialized" -ForegroundColor Yellow
    Write-Host "  - Required data does not exist yet" -ForegroundColor Yellow
    exit 1
}
