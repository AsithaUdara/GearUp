# ========================================
# GearUp Backend - Modification Service Test (PowerShell)
# ========================================
# This script tests the modification service endpoints

# Configuration
$BASE_URL = "http://localhost:8089"
$SERVICE_ID = 1
$CUSTOMER_ID = 1

Write-Host "🔍 Testing Modification Service at $BASE_URL" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Test 1: Get all modification services
Write-Host "1. Testing GET /api/modifications/services" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/modifications/services" -Method GET
    Write-Host "✅ Services endpoint works: $($response.Count) services found" -ForegroundColor Green
} catch {
    Write-Host "❌ Services endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get active modification services
Write-Host "`n2. Testing GET /api/modifications/services/active" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/modifications/services/active" -Method GET
    Write-Host "✅ Active services endpoint works: $($response.Count) active services found" -ForegroundColor Green
} catch {
    Write-Host "❌ Active services endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create a modification request
Write-Host "`n3. Testing POST /api/modification-requests" -ForegroundColor Yellow
$requestData = @{
    serviceId = $SERVICE_ID
    customerId = $CUSTOMER_ID
    preferredDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    notes = "Test modification request - Engine performance upgrade"
} | ConvertTo-Json

try {
    $requestResponse = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests" -Method POST -Body $requestData -ContentType "application/json"
    $REQUEST_ID = $requestResponse.id
    Write-Host "✅ Modification request created successfully: ID $REQUEST_ID" -ForegroundColor Green
    
    # Test 4: Get modification request by ID
    Write-Host "`n4. Testing GET /api/modification-requests/$REQUEST_ID" -ForegroundColor Yellow
    try {
        $request = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests/$REQUEST_ID" -Method GET
        Write-Host "✅ Modification request retrieved: Status $($request.status)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get modification request failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 5: Get all modification requests
    Write-Host "`n5. Testing GET /api/modification-requests" -ForegroundColor Yellow
    try {
        $allRequests = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests" -Method GET
        Write-Host "✅ All modification requests retrieved: $($allRequests.Count) requests found" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get all modification requests failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 6: Update modification request
    Write-Host "`n6. Testing PUT /api/modification-requests/$REQUEST_ID" -ForegroundColor Yellow
    $updateData = @{
        notes = "Updated test modification request"
        adminNotes = "Reviewed by admin"
    } | ConvertTo-Json
    
    try {
        $updatedRequest = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests/$REQUEST_ID" -Method PUT -Body $updateData -ContentType "application/json"
        Write-Host "✅ Modification request updated successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Modification request update failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 7: Approve modification request
    Write-Host "`n7. Testing PUT /api/modification-requests/$REQUEST_ID/approve" -ForegroundColor Yellow
    $approveData = @{
        estimatedCost = 1500.00
    } | ConvertTo-Json
    
    try {
        $approvedRequest = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests/$REQUEST_ID/approve" -Method PUT -Body $approveData -ContentType "application/json"
        Write-Host "✅ Modification request approved successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Modification request approval failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 8: Get requests by status
    Write-Host "`n8. Testing GET /api/modification-requests/status/APPROVED" -ForegroundColor Yellow
    try {
        $approvedRequests = Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests/status/APPROVED" -Method GET
        Write-Host "✅ Approved requests retrieved: $($approvedRequests.Count) approved requests found" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get requests by status failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test 9: Delete modification request
    Write-Host "`n9. Testing DELETE /api/modification-requests/$REQUEST_ID" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/modification-requests/$REQUEST_ID" -Method DELETE
        Write-Host "✅ Modification request deleted successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Modification request deletion failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Modification request creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️  Skipping remaining modification request tests" -ForegroundColor Yellow
}

Write-Host "`n✅ Modification service test completed!" -ForegroundColor Green
