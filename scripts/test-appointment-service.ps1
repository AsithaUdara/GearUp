# ========================================
# GearUp Backend - Appointment Service Test (PowerShell)
# ========================================
# This script tests the appointment service endpoints

# Configuration
$BASE_URL = "http://localhost:8084"
$SERVICE_ID = 1
$USER_ID = "test_user_123"
$DATE = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")

Write-Host "🔍 Testing Appointment Service at $BASE_URL" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Test 1: Get all services
Write-Host "1. Testing GET /api/services" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/services" -Method GET
    Write-Host "✅ Services endpoint works: $($response.Count) services found" -ForegroundColor Green
} catch {
    Write-Host "❌ Services endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get time slots
Write-Host "`n2. Testing GET /api/timeslots?serviceId=$SERVICE_ID&date=$DATE" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/timeslots?serviceId=$SERVICE_ID&date=$DATE" -Method GET
    Write-Host "✅ Timeslots endpoint works: $($response.Count) slots found" -ForegroundColor Green
} catch {
    Write-Host "❌ Timeslots endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create a booking
Write-Host "`n3. Testing POST /api/bookings" -ForegroundColor Yellow
$bookingData = @{
    serviceId = $SERVICE_ID
    timeSlotId = 1
    userId = $USER_ID
    customerName = "John Doe"
    customerEmail = "john.doe@example.com"
    customerPhone = "+1234567890"
    notes = "Test booking"
} | ConvertTo-Json

try {
    $bookingResponse = Invoke-RestMethod -Uri "$BASE_URL/api/bookings" -Method POST -Body $bookingData -ContentType "application/json"
    $BOOKING_ID = $bookingResponse.id
    Write-Host "✅ Booking created successfully: ID $BOOKING_ID" -ForegroundColor Green
    
    # Test 4: Get user bookings
    Write-Host "`n4. Testing GET /api/bookings?userId=$USER_ID" -ForegroundColor Yellow
    try {
        $userBookings = Invoke-RestMethod -Uri "$BASE_URL/api/bookings?userId=$USER_ID" -Method GET
        Write-Host "✅ User bookings retrieved: $($userBookings.Count) bookings found" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get user bookings failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 5: Update booking
    Write-Host "`n5. Testing PUT /api/bookings/$BOOKING_ID" -ForegroundColor Yellow
    $updateData = @{
        customerName = "John Smith"
        notes = "Updated test booking"
    } | ConvertTo-Json
    
    try {
        $updatedBooking = Invoke-RestMethod -Uri "$BASE_URL/api/bookings/$BOOKING_ID" -Method PUT -Body $updateData -ContentType "application/json"
        Write-Host "✅ Booking updated successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Booking update failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 6: Cancel booking
    Write-Host "`n6. Testing DELETE /api/bookings/$BOOKING_ID" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/bookings/$BOOKING_ID" -Method DELETE
        Write-Host "✅ Booking cancelled successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Booking cancellation failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Booking creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️  Skipping remaining booking tests" -ForegroundColor Yellow
}

Write-Host "`n✅ Appointment service test completed!" -ForegroundColor Green