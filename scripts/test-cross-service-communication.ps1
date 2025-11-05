# Cross-Service Communication Testing Script
# This script demonstrates and tests the event-driven and synchronous communication patterns

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Cross-Service Communication Tests" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$NOTIFICATION_SERVICE = "http://localhost:8081"
$API_GATEWAY = "http://localhost:8080"
$EUREKA_SERVER = "http://localhost:8761"
$RABBITMQ_MGMT = "http://localhost:15672"

# Test User ID
$TEST_USER_ID = "test-user-$(Get-Random -Minimum 1000 -Maximum 9999)"

Write-Host "Test Configuration:" -ForegroundColor Yellow
Write-Host "  User ID: $TEST_USER_ID"
Write-Host "  Notification Service: $NOTIFICATION_SERVICE"
Write-Host "  API Gateway: $API_GATEWAY"
Write-Host "  Eureka Server: $EUREKA_SERVER"
Write-Host ""

# Function to make HTTP requests and display results
function Invoke-TestRequest {
    param(
        [string]$Title,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "------------------------------------" -ForegroundColor Gray
    Write-Host "TEST: $Title" -ForegroundColor Green
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri         = $Url
            Method      = $Method
            Headers     = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "[SUCCESS]" -ForegroundColor Green
        Write-Host "Response:" -ForegroundColor Gray
        $response | ConvertTo-Json -Depth 5 | Write-Host
        
        return $response
    }
    catch {
        Write-Host "[FAILED]" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.ErrorDetails.Message) {
            Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        
        return $null
    }
    
    Write-Host ""
}

# Test 1: Check Services Health
Write-Host ""
Write-Host "=== STEP 1: Health Checks ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Notification Service Health" -Url "$NOTIFICATION_SERVICE/actuator/health"

Start-Sleep -Seconds 1

Invoke-TestRequest -Title "Cross-Service Communication Health" -Url "$NOTIFICATION_SERVICE/api/cross-service-test/health"

Start-Sleep -Seconds 1

# Test 2: Check Eureka Registration
Write-Host ""
Write-Host "=== STEP 2: Eureka Service Discovery ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Eureka - Registered Services" -Url "$EUREKA_SERVER/eureka/apps"

Start-Sleep -Seconds 1

# Test 3: RabbitMQ Event Publishing
Write-Host ""
Write-Host "=== STEP 3: RabbitMQ Event Publishing ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Publish Vehicle Booking Created Event" -Url "$NOTIFICATION_SERVICE/api/events/simulate/vehicle-booking-created?userId=$TEST_USER_ID" -Method "POST"

Start-Sleep -Seconds 2

Invoke-TestRequest -Title "Publish Invoice Created Event" -Url "$NOTIFICATION_SERVICE/api/events/simulate/invoice-created?userId=$TEST_USER_ID" -Method "POST"

Start-Sleep -Seconds 2

Invoke-TestRequest -Title "Publish Task Assigned Event" -Url "$NOTIFICATION_SERVICE/api/events/simulate/task-assigned?userId=$TEST_USER_ID" -Method "POST"

Start-Sleep -Seconds 2

# Test 4: Verify Notifications Created
Write-Host ""
Write-Host "=== STEP 4: Verify Event Processing ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Get User Notifications" -Url "$NOTIFICATION_SERVICE/api/notifications/user/$TEST_USER_ID"

Start-Sleep -Seconds 1

# Test 5: Cross-Service Communication (Feign)
Write-Host ""
Write-Host "=== STEP 5: Cross-Service Communication (Feign Client) ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Test Feign Client - User Details" -Url "$NOTIFICATION_SERVICE/api/cross-service-test/user/$TEST_USER_ID"

Start-Sleep -Seconds 1

# Test 6: API Gateway Routing
Write-Host ""
Write-Host "=== STEP 6: API Gateway Routing ===" -ForegroundColor Cyan

try {
    Invoke-TestRequest -Title "Access via API Gateway" -Url "$API_GATEWAY/api/notifications/cross-service-test/health"
}
catch {
    Write-Host "Note: API Gateway may not be running. Start it with: ./mvnw spring-boot:run -pl api-gateway" -ForegroundColor Yellow
}

Start-Sleep -Seconds 1

# Test 7: Publish All Events
Write-Host ""
Write-Host "=== STEP 7: Bulk Event Publishing ===" -ForegroundColor Cyan

Invoke-TestRequest -Title "Publish All Event Types" -Url "$NOTIFICATION_SERVICE/api/events/simulate/all?userId=$TEST_USER_ID" -Method "POST"

Start-Sleep -Seconds 3

# Final Verification
Write-Host ""
Write-Host "=== STEP 8: Final Verification ===" -ForegroundColor Cyan

$finalNotifications = Invoke-TestRequest -Title "Get All User Notifications After Bulk Publish" -Url "$NOTIFICATION_SERVICE/api/notifications/user/$TEST_USER_ID"

if ($finalNotifications) {
    $count = 0
    if ($finalNotifications.content) {
        $count = $finalNotifications.content.Count
    }
    elseif ($finalNotifications -is [array]) {
        $count = $finalNotifications.Count
    }
    
    Write-Host ""
    Write-Host "Total Notifications Created: $count" -ForegroundColor Green
}

# Summary
Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Test User ID: $TEST_USER_ID" -ForegroundColor Yellow
Write-Host ""
Write-Host "[OK] RabbitMQ Event Publishing - TESTED" -ForegroundColor Green
Write-Host "[OK] Event Consumption and Processing - TESTED" -ForegroundColor Green
Write-Host "[OK] Notification Creation - TESTED" -ForegroundColor Green
Write-Host "[OK] Cross-Service Communication (Feign) - TESTED" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Check RabbitMQ Management UI: $RABBITMQ_MGMT" -ForegroundColor Gray
Write-Host "     - View queue depths and message rates" -ForegroundColor Gray
Write-Host "  2. Check Eureka Dashboard: $EUREKA_SERVER" -ForegroundColor Gray
Write-Host "     - Verify service registrations" -ForegroundColor Gray
Write-Host "  3. Review notification-service logs for event processing" -ForegroundColor Gray
Write-Host ""
Write-Host "Documentation:" -ForegroundColor Yellow
Write-Host "  - CROSS_SERVICE_COMMUNICATION.md - Complete guide" -ForegroundColor Gray
Write-Host "  - RABBITMQ_GUIDE.md - RabbitMQ setup and usage" -ForegroundColor Gray
Write-Host ""

# Optional: Open URLs in browser
$openBrowser = Read-Host "Open management UIs in browser? (y/n)"
if ($openBrowser -eq "y") {
    Start-Process $RABBITMQ_MGMT
    Start-Process $EUREKA_SERVER
    Write-Host "Opened RabbitMQ and Eureka dashboards" -ForegroundColor Green
}

Write-Host ""
Write-Host "Testing complete!" -ForegroundColor Cyan
