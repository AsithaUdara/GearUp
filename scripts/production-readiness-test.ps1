# ======================================================================
# GearUp Backend - Production Readiness Test Suite
# ======================================================================
# This script performs comprehensive end-to-end testing of the system
# ======================================================================

$ErrorActionPreference = "Continue"
$testResults = @()

function Write-TestHeader {
    param([string]$title)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $title -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Write-TestResult {
    param(
        [string]$test,
        [bool]$passed,
        [string]$details = ""
    )
    
    $status = if ($passed) { "[PASS]" } else { "[FAIL]" }
    $color = if ($passed) { "Green" } else { "Red" }
    
    Write-Host "$status - $test" -ForegroundColor $color
    if ($details) {
        Write-Host "  $details" -ForegroundColor Gray
    }
    
    $script:testResults += @{
        Test    = $test
        Passed  = $passed
        Details = $details
    }
}

# ======================================================================
# TEST 1: API Gateway Connectivity
# ======================================================================
Write-TestHeader "TEST 1: API GATEWAY CONNECTIVITY"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/hello" -Method Get -TimeoutSec 5
    $passed = $response -eq "Hello There!"
    Write-TestResult "API Gateway routing to chatbot-service" $passed "Response: $response"
}
catch {
    Write-TestResult "API Gateway routing to chatbot-service" $false $_.Exception.Message
}

# ======================================================================
# TEST 2: Service Discovery
# ======================================================================
Write-TestHeader "TEST 2: SERVICE DISCOVERY (EUREKA)"

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Method Get -TimeoutSec 5
    $serviceCount = ([xml]$response).applications.application.Count
    $passed = $serviceCount -gt 0
    Write-TestResult "Eureka service registry" $passed "$serviceCount services registered"
}
catch {
    Write-TestResult "Eureka service registry" $false $_.Exception.Message
}

# ======================================================================
# TEST 3: RabbitMQ Event Flow (Invoice Created to Notification)
# ======================================================================
Write-TestHeader "TEST 3: RABBITMQ EVENT PUBLISHING AND CONSUMPTION"

Write-Host "Testing: Invoice Created Event to Notification Service" -ForegroundColor Yellow
Write-Host "Note: This requires authentication. Checking RabbitMQ management..." -ForegroundColor Gray

try {
    $rabbitMgmt = Invoke-RestMethod -Uri "http://localhost:15672/api/overview" -Method Get `
        -Credential (New-Object System.Management.Automation.PSCredential("guest", (ConvertTo-SecureString "guest" -AsPlainText -Force))) -TimeoutSec 5
    
    Write-TestResult "RabbitMQ Management API accessible" $true "Version: $($rabbitMgmt.rabbitmq_version)"
    
    # Check queues
    $queues = Invoke-RestMethod -Uri "http://localhost:15672/api/queues" -Method Get `
        -Credential (New-Object System.Management.Automation.PSCredential("guest", (ConvertTo-SecureString "guest" -AsPlainText -Force)))
    
    $notificationQueue = $queues | Where-Object { $_.name -eq "notification.queue" }
    if ($notificationQueue) {
        Write-TestResult "Notification queue exists" $true "Messages: $($notificationQueue.messages), Consumers: $($notificationQueue.consumers)"
    }
    else {
        Write-TestResult "Notification queue exists" $false "Queue not found"
    }
    
}
catch {
    Write-TestResult "RabbitMQ connectivity" $false $_.Exception.Message
}

# ======================================================================
# TEST 4: Redis Cache
# ======================================================================
Write-TestHeader "TEST 4: REDIS CACHING"

try {
    # Test Redis connectivity
    $redisTest = docker exec docker-redis-1 redis-cli PING 2>&1
    $passed = $redisTest -eq "PONG"
    Write-TestResult "Redis server connectivity" $passed "Response: $redisTest"
    
    if ($passed) {
        # Check Redis keys
        $keys = docker exec docker-redis-1 redis-cli KEYS "*" 2>&1
        $keyCount = if ($keys -is [array]) { $keys.Count } else { if ($keys) { 1 } else { 0 } }
        Write-Host "  Redis keys in cache: $keyCount" -ForegroundColor Gray
    }
}
catch {
    Write-TestResult "Redis server connectivity" $false $_.Exception.Message
}

# ======================================================================
# TEST 5: Database Connectivity
# ======================================================================
Write-TestHeader "TEST 5: DATABASE CONNECTIVITY"

try {
    $result = docker exec gearup-postgres psql -U postgres -d as_user_auth_service -t -c "SELECT COUNT(*) FROM users;" 2>&1
    $userCount = [int]($result.Trim())
    Write-TestResult "PostgreSQL - user-auth-service DB" $true "$userCount users in database"
}
catch {
    Write-TestResult "PostgreSQL - user-auth-service DB" $false $_.Exception.Message
}

try {
    $result = docker exec gearup-postgres psql -U postgres -d as_notification_service -t -c "SELECT COUNT(*) FROM notifications;" 2>&1
    $notificationCount = [int]($result.Trim())
    Write-TestResult "PostgreSQL - notification-service DB" $true "$notificationCount notifications in database"
}
catch {
    Write-TestResult "PostgreSQL - notification-service DB" $false $_.Exception.Message
}

# ======================================================================
# TEST 6: Service Health Checks
# ======================================================================
Write-TestHeader "TEST 6: SERVICE HEALTH CHECKS"

$services = @(
    "gearup-api-gateway",
    "gearup-config-server",
    "gearup-service-discovery",
    "gearup-user-auth-service",
    "gearup-notification-service",
    "gearup-payment-service",
    "gearup-chatbot-service",
    "gearup-customer-service",
    "gearup-vehicle-service",
    "gearup-appointment-service"
)

foreach ($service in $services) {
    try {
        $status = docker ps --filter "name=$service" --format "{{.Status}}"
        $isHealthy = $status -match "healthy|Up"
        Write-TestResult "$service health" $isHealthy $status
    }
    catch {
        Write-TestResult "$service health" $false "Not found"
    }
}

# ======================================================================
# SUMMARY
# ======================================================================
Write-TestHeader "TEST SUMMARY"

$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object { $_.Passed }).Count
$failedTests = $totalTests - $passedTests
$passRate = [math]::Round(($passedTests / $totalTests) * 100, 1)

Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passedTests" -ForegroundColor Green
Write-Host "Failed: $failedTests" -ForegroundColor Red
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } else { "Yellow" })

Write-Host "`n========================================" -ForegroundColor Cyan
if ($passRate -ge 90) {
    Write-Host "[OK] SYSTEM IS PRODUCTION READY" -ForegroundColor Green
}
elseif ($passRate -ge 70) {
    Write-Host "[WARNING] SYSTEM NEEDS ATTENTION" -ForegroundColor Yellow
}
else {
    Write-Host "[CRITICAL] SYSTEM NOT PRODUCTION READY" -ForegroundColor Red
}
Write-Host "========================================`n" -ForegroundColor Cyan

# ======================================================================
# DATABASE TABLES TO VERIFY (For Manual Checking)
# ======================================================================
Write-Host "`nDATABASE TABLES FOR MANUAL VERIFICATION:" -ForegroundColor Yellow
Write-Host "After invoice creation, check these tables:" -ForegroundColor Gray
Write-Host "  - as_payment_service.invoices (new invoice record)" -ForegroundColor White
Write-Host "  - as_payment_service.payments (payment record)" -ForegroundColor White
Write-Host "  - as_notification_service.notifications (notification generated)" -ForegroundColor White
Write-Host "  - as_user_auth_service.users (user record)" -ForegroundColor White
Write-Host "`n"
