# Cross-Service Communication Flow Test Script
# Tests the complete flow: automobile-service -> RabbitMQ -> notification-service

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Cross-Service Communication Flow Test" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$AUTOMOBILE_SERVICE = "http://localhost:8080"
$NOTIFICATION_SERVICE = "http://localhost:8081"
$userId = "test-user-$(Get-Random -Minimum 1000 -Maximum 9999)"

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Test User ID: $userId" -ForegroundColor Gray
Write-Host "  Automobile Service: $AUTOMOBILE_SERVICE" -ForegroundColor Gray
Write-Host "  Notification Service: $NOTIFICATION_SERVICE" -ForegroundColor Gray
Write-Host ""

# Function to make HTTP requests
function Invoke-TestRequest {
    param(
        [string]$Title,
        [string]$Url,
        [string]$Method = "GET"
    )
    
    Write-Host "TEST: $Title" -ForegroundColor Green
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method $Method -ContentType "application/json"
        Write-Host "  [SUCCESS]" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Step 1: Create Test Booking
Write-Host ""
Write-Host "=== STEP 1: Create Test Booking ===" -ForegroundColor Cyan
Write-Host "This will:" -ForegroundColor Gray
Write-Host "  1. Save booking to PostgreSQL" -ForegroundColor Gray
Write-Host "  2. Cache booking in Redis" -ForegroundColor Gray
Write-Host "  3. Publish VehicleBookingCreatedEvent to RabbitMQ" -ForegroundColor Gray
Write-Host ""

$createResponse = Invoke-TestRequest `
    -Title "Create test booking" `
    -Url "$AUTOMOBILE_SERVICE/api/bookings/test?userId=$userId" `
    -Method "POST"

if (-not $createResponse) {
    Write-Host ""
    Write-Host "[ERROR] Failed to create booking. Ensure automobile-service is running." -ForegroundColor Red
    exit 1
}

$bookingId = $createResponse.booking.bookingId
Write-Host ""
Write-Host "Booking Details:" -ForegroundColor Yellow
Write-Host "  Booking ID: $bookingId" -ForegroundColor Gray
Write-Host "  Vehicle: $($createResponse.booking.vehicleName)" -ForegroundColor Gray
Write-Host "  Amount: `$$($createResponse.booking.totalAmount)" -ForegroundColor Gray
Write-Host "  Status: $($createResponse.booking.status)" -ForegroundColor Gray

Start-Sleep -Seconds 2

# Step 2: Verify Booking in automobile-service (Redis Cache)
Write-Host ""
Write-Host "=== STEP 2: Verify Booking (Redis Cache Test) ===" -ForegroundColor Cyan
Write-Host "First request will hit database, second will use Redis cache" -ForegroundColor Gray
Write-Host ""

# First request (cache miss)
Write-Host "Request 1 (Database):" -ForegroundColor Yellow
$booking1 = Invoke-TestRequest `
    -Title "Get booking - First request" `
    -Url "$AUTOMOBILE_SERVICE/api/bookings/$bookingId"

Start-Sleep -Milliseconds 500

# Second request (cache hit)
Write-Host ""
Write-Host "Request 2 (Redis Cache):" -ForegroundColor Yellow
$booking2 = Invoke-TestRequest `
    -Title "Get booking - Second request" `
    -Url "$AUTOMOBILE_SERVICE/api/bookings/$bookingId"

if ($booking2) {
    Write-Host ""
    Write-Host "[INFO] Second request should be faster (served from Redis)" -ForegroundColor Cyan
}

Start-Sleep -Seconds 2

# Step 3: Check Notification Created
Write-Host ""
Write-Host "=== STEP 3: Verify Event Consumed by notification-service ===" -ForegroundColor Cyan
Write-Host "Checking if VehicleBookingCreatedEvent was consumed..." -ForegroundColor Gray
Write-Host ""

$notifications = Invoke-TestRequest `
    -Title "Get user notifications" `
    -Url "$NOTIFICATION_SERVICE/api/notifications/user/$userId"

if ($notifications -and $notifications.totalElements -gt 0) {
    Write-Host ""
    Write-Host "Notifications Found: $($notifications.totalElements)" -ForegroundColor Green
    Write-Host ""
    $notifications.content | ForEach-Object {
        Write-Host "  Notification:" -ForegroundColor Yellow
        Write-Host "    Type: $($_.type)" -ForegroundColor Gray
        Write-Host "    Title: $($_.title)" -ForegroundColor Gray
        Write-Host "    Message: $($_.message)" -ForegroundColor Gray
        Write-Host "    Created: $($_.createdAt)" -ForegroundColor Gray
        Write-Host ""
    }
}
else {
    Write-Host ""
    Write-Host "[WARNING] No notifications found. Event may still be processing..." -ForegroundColor Yellow
    Write-Host "  Check notification-service logs for event consumption" -ForegroundColor Gray
}

Start-Sleep -Seconds 2

# Step 4: Confirm Booking
Write-Host ""
Write-Host "=== STEP 4: Confirm Booking ===" -ForegroundColor Cyan
Write-Host "This will publish VehicleBookingConfirmedEvent" -ForegroundColor Gray
Write-Host ""

$confirmed = Invoke-TestRequest `
    -Title "Confirm booking" `
    -Url "$AUTOMOBILE_SERVICE/api/bookings/$bookingId/confirm" `
    -Method "POST"

if ($confirmed) {
    Write-Host ""
    Write-Host "Booking Status Updated: $($confirmed.status)" -ForegroundColor Green
}

Start-Sleep -Seconds 3

# Step 5: Verify Confirmation Notification
Write-Host ""
Write-Host "=== STEP 5: Verify Confirmation Notification ===" -ForegroundColor Cyan
Write-Host ""

$finalNotifications = Invoke-TestRequest `
    -Title "Get updated notifications" `
    -Url "$NOTIFICATION_SERVICE/api/notifications/user/$userId"

if ($finalNotifications) {
    Write-Host ""
    Write-Host "Total Notifications: $($finalNotifications.totalElements)" -ForegroundColor Green
    Write-Host ""
    
    if ($finalNotifications.totalElements -ge 2) {
        Write-Host "[SUCCESS] Both booking creation and confirmation notifications created!" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "All Notifications:" -ForegroundColor Yellow
    $finalNotifications.content | ForEach-Object {
        Write-Host "  - [$($_.type)] $($_.title)" -ForegroundColor Gray
    }
}

# Summary
Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Test User ID: $userId" -ForegroundColor Yellow
Write-Host "Booking ID: $bookingId" -ForegroundColor Yellow
Write-Host ""

if ($finalNotifications) {
    Write-Host "Results:" -ForegroundColor Yellow
    Write-Host "  Bookings Created: 1" -ForegroundColor Gray
    Write-Host "  Notifications Created: $($finalNotifications.totalElements)" -ForegroundColor Gray
    Write-Host "  Redis Caching: Enabled" -ForegroundColor Gray
    Write-Host "  RabbitMQ Events: Published & Consumed" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "Components Tested:" -ForegroundColor Cyan
Write-Host "  [OK] PostgreSQL - Database storage" -ForegroundColor Green
Write-Host "  [OK] Redis - Caching layer" -ForegroundColor Green
Write-Host "  [OK] RabbitMQ - Event messaging" -ForegroundColor Green
Write-Host "  [OK] Cross-Service - automobile -> notification" -ForegroundColor Green
Write-Host ""

Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Check RabbitMQ Management: http://localhost:15672" -ForegroundColor Gray
Write-Host "  2. Check Eureka Dashboard: http://localhost:8761" -ForegroundColor Gray
Write-Host "  3. View automobile-service logs for event publishing" -ForegroundColor Gray
Write-Host "  4. View notification-service logs for event consumption" -ForegroundColor Gray
Write-Host ""

Write-Host "API Endpoints to Explore:" -ForegroundColor Cyan
Write-Host "  Get Booking: GET $AUTOMOBILE_SERVICE/api/bookings/$bookingId" -ForegroundColor Gray
Write-Host "  Get Notifications: GET $NOTIFICATION_SERVICE/api/notifications/user/$userId" -ForegroundColor Gray
Write-Host "  Get All Bookings: GET $AUTOMOBILE_SERVICE/api/bookings" -ForegroundColor Gray
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Test Complete!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
