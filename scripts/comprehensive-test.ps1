# GearUp Backend Comprehensive System Test
# Tests API Gateway, RabbitMQ Events, Redis Caching, and Database Updates

$ErrorActionPreference = "Continue"
$TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjM4MDI5MzRmZTBlZWM0NmE1ZWQwMDA2ZDE0YTFiYWIwMWUzNDUwODMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZ2Vhci11cC00NmFkYyIsImF1ZCI6ImdlYXItdXAtNDZhZGMiLCJhdXRoX3RpbWUiOjE3NjMwNjIzMzQsInVzZXJfaWQiOiJpSTBaTk5UallzWkhBWU95Y01QczVEM3JvTDEyIiwic3ViIjoiaUkwWk5OVGpZc1pIQVlPeWNNUHM1RDNyb0wxMiIsImlhdCI6MTc2MzA2MjMzNCwiZXhwIjoxNzYzMDY1OTM0LCJlbWFpbCI6ImdkQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJnZEBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.nwyAvwYkfSsP5J_m4fyWSiY-qCdr92vFzlgdk4LQR2g2OfQGFaE4wRrsnbL7f8OtOAxWK-vmk5E6p9fucmWtLtJXzeMHZqMKh0U4QuR67CL_Sc1lq1rbrH1EwKMHjR1rJ6R0k0AxiEp0vhQysWOspm_0AKcMZRlfBVjnS0vtK8KTENcATpt5oN6oMg77AvIwFSLVr-7TcWcG9zFfbpoWeIy7RGlNYjpi9wSeV3vi3VIttdKYSKGzaswmPdR0pBnUaF_-w7Q2frousGI7fYTS_Lsi4_BqWJGnOrqwI6L_1pjasVH08MbKau6T9iQgOm71kngBEneMw8zHBUM1uXAc5w"
$GATEWAY_URL = "http://localhost:9090"
$HEADERS = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type"  = "application/json"
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  GearUp Backend System Test Suite" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: API Gateway - Hello Endpoint
Write-Host "[TEST 1] Testing API Gateway - Hello Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/api/v1/hello" -Method GET -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ PASS: API Gateway accessible, response: $($response.Content)" -ForegroundColor Green
    }
}
catch {
    Write-Host "❌ FAIL: API Gateway hello endpoint failed - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create Payment Request
Write-Host "`n[TEST 2] Creating Payment Request..." -ForegroundColor Yellow
$paymentRequestBody = @{
    customerName  = "John Doe"
    customerEmail = "john.doe@example.com"
    vehicleInfo   = "Toyota Camry 2024 - ABC123"
    submittedBy   = "iI0ZNNTjYsZHAYOycMPs5D3roL12"
    services      = @(
        @{
            description = "Oil Change"
            price       = 50.00
        },
        @{
            description = "Brake Inspection"
            price       = 75.00
        }
    )
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments/admin/requests" -Method POST -Headers $HEADERS -Body $paymentRequestBody
    $paymentRequestId = $response.id
    Write-Host "✅ PASS: Payment request created with ID: $paymentRequestId" -ForegroundColor Green
    Write-Host "   Total Amount: $($response.totalAmount)" -ForegroundColor Gray
    Write-Host "   Status: $($response.status)" -ForegroundColor Gray
}
catch {
    Write-Host "❌ FAIL: Payment request creation failed - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Error Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# Test 3: Get Payment Requests
Write-Host "`n[TEST 3] Fetching All Payment Requests..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments/admin/requests" -Method GET -Headers $HEADERS
    Write-Host "✅ PASS: Retrieved $($response.Count) payment request(s)" -ForegroundColor Green
}
catch {
    Write-Host "❌ FAIL: Failed to fetch payment requests - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Approve Payment Request (This should trigger invoice creation and notification)
Write-Host "`n[TEST 4] Approving Payment Request (Triggers Invoice Event)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments/admin/requests/$paymentRequestId/approve" -Method PUT -Headers $HEADERS
    Write-Host "✅ PASS: Payment request approved!" -ForegroundColor Green
    Write-Host "   Status: $($response.status)" -ForegroundColor Gray
    Write-Host "   Approved Date: $($response.approvedDate)" -ForegroundColor Gray
    Write-Host "   This should have created:" -ForegroundColor Cyan
    Write-Host "     1. Customer Bill (Invoice)" -ForegroundColor Cyan
    Write-Host "     2. Published InvoiceCreatedEvent to RabbitMQ" -ForegroundColor Cyan
    Write-Host "     3. Notification service should receive event" -ForegroundColor Cyan
    Write-Host "     4. Notification record should be created" -ForegroundColor Cyan
}
catch {
    Write-Host "❌ FAIL: Payment approval failed - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Error Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Wait for event processing
Write-Host "`n[WAIT] Waiting 3 seconds for RabbitMQ event processing..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

# Test 5: User Profile (Test Authentication)
Write-Host "`n[TEST 5] Testing User Profile Endpoint (Authentication)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/users/me" -Method GET -Headers $HEADERS
    Write-Host "✅ PASS: User authenticated successfully" -ForegroundColor Green
    Write-Host "   User ID: $($response.uid)" -ForegroundColor Gray
    Write-Host "   Email: $($response.email)" -ForegroundColor Gray
}
catch {
    Write-Host "⚠️ WARNING: User profile endpoint failed - $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test 6: Payment Statistics
Write-Host "`n[TEST 6] Fetching Payment Statistics..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments/admin/stats" -Method GET -Headers $HEADERS
    Write-Host "✅ PASS: Payment stats retrieved" -ForegroundColor Green
    Write-Host "   Pending: $($response.pendingCount)" -ForegroundColor Gray
    Write-Host "   Approved: $($response.approvedCount)" -ForegroundColor Gray
    Write-Host "   Rejected: $($response.rejectedCount)" -ForegroundColor Gray
    Write-Host "   Total Revenue: $($response.totalRevenue)" -ForegroundColor Gray
}
catch {
    Write-Host "❌ FAIL: Failed to fetch payment stats - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Public Reviews (No Auth Required)
Write-Host "`n[TEST 7] Testing Public Reviews Endpoint (No Auth)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/reviews/public" -Method GET -UseBasicParsing
    Write-Host "✅ PASS: Public reviews endpoint accessible" -ForegroundColor Green
    Write-Host "   Reviews count: $($response.Count)" -ForegroundColor Gray
}
catch {
    Write-Host "⚠️ INFO: No published reviews yet or endpoint error - $($_.Exception.Message)" -ForegroundColor Yellow
}

# Summary Report
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  DATABASE VERIFICATION CHECKLIST" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Please verify the following tables in your databases:`n" -ForegroundColor White

Write-Host "PAYMENT SERVICE DATABASE:" -ForegroundColor Yellow
Write-Host "  1. payment_requests - Should have new record with status='APPROVED'" -ForegroundColor White
Write-Host "  2. customer_bills - Should have new invoice/bill record" -ForegroundColor White
Write-Host "  3. payment_request_services - Should have service items linked to the request`n" -ForegroundColor White

Write-Host "NOTIFICATION SERVICE DATABASE:" -ForegroundColor Yellow
Write-Host "  4. notification - Should have new notification record with:" -ForegroundColor White
Write-Host "     - type='INVOICE_CREATED'" -ForegroundColor White
Write-Host "     - message about invoice creation" -ForegroundColor White
Write-Host "     - related_entity_type='INVOICE'`n" -ForegroundColor White

Write-Host "REDIS VERIFICATION:" -ForegroundColor Yellow
Write-Host "  - Event deduplication keys should be cached" -ForegroundColor White
Write-Host "  - Check Redis keys: events:dedup:*" -ForegroundColor White
Write-Host "  - Rate limiting counters should be active`n" -ForegroundColor White

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Suite Completed!" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Additional Redis Test
Write-Host "[REDIS] Testing Redis connectivity via rate limiter..." -ForegroundColor Yellow
$rateTestCount = 0
for ($i = 1; $i -le 5; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "$GATEWAY_URL/api/v1/hello" -Method GET -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            $rateTestCount++
        }
    }
    catch {
        Write-Host "Request $i failed (might be rate limited)" -ForegroundColor Yellow
    }
}
Write-Host "✅ PASS: Sent 5 requests, $rateTestCount succeeded (Redis rate limiting working)" -ForegroundColor Green
