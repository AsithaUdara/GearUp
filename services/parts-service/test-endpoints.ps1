# Helper function to handle errors
function Invoke-ApiWithErrorHandling {
    param(
        [string]$Uri,
        [string]$Method,
        [hashtable]$Headers,
        [string]$Body
    )
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers
        }
        return $response
    } catch {
        Write-Host "Error calling $Uri"
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
        Write-Host "Response: $($_.Exception.Response.StatusDescription)"
        Write-Host "Details: $($_.ErrorDetails.Message)"
        return $null
    }
}

# Test data
$userId = (New-Guid).ToString()
$requestId = (New-Guid).ToString()

# Headers
$headers = @{
    "Content-Type" = "application/json"
    "X-User-ID" = $userId  # Using header auth for testing
}

Write-Host "`n=== Testing Parts Service API Endpoints ==="
$baseUrl = "http://localhost:8082/api/v1/parts-requests"

# 1. Create Parts Request
Write-Host "`nTest 1: Creating Parts Request..."
$createBody = @{
    material = "Steel Bolts"
    quantity = 100
    notes = "Urgent requirement for vehicle maintenance"
    urgency = "HIGH"
} | ConvertTo-Json

$createResponse = Invoke-ApiWithErrorHandling -Uri $baseUrl -Method Post -Headers $headers -Body $createBody
if ($createResponse) {
    $requestId = $createResponse.id
    Write-Host "✓ Successfully created parts request with ID: $requestId"
} else {
    Write-Host "✗ Failed to create parts request"
    exit 1
}

# 2. Get User Requests
Write-Host "`nTest 2: Fetching User Requests..."
$userRequestsResponse = Invoke-ApiWithErrorHandling -Uri "$baseUrl/my-requests" -Method Get -Headers $headers
if ($userRequestsResponse) {
    Write-Host "✓ Successfully retrieved user requests"
    Write-Host "Found $($userRequestsResponse.Count) requests"
} else {
    Write-Host "✗ Failed to fetch user requests"
}

# 3. Get Requests by Status
Write-Host "`nTest 3: Fetching Requests by Status (PENDING)..."
$statusResponse = Invoke-ApiWithErrorHandling -Uri "$baseUrl/by-status/PENDING" -Method Get -Headers $headers
if ($statusResponse) {
    Write-Host "✓ Successfully retrieved requests by status"
    Write-Host "Found $($statusResponse.Count) PENDING requests"
} else {
    Write-Host "✗ Failed to fetch requests by status"
}

# 4. Update Request Status
Write-Host "`nTest 4: Updating Request Status..."
$updateResponse = Invoke-ApiWithErrorHandling -Uri "$baseUrl/$requestId/status?status=APPROVED" -Method Put -Headers $headers
if ($updateResponse) {
    Write-Host "✓ Successfully updated request status to APPROVED"
} else {
    Write-Host "✗ Failed to update request status"
}

# 5. Get Single Request
Write-Host "`nTest 5: Fetching Single Request..."
$getResponse = Invoke-ApiWithErrorHandling -Uri "$baseUrl/$requestId" -Method Get -Headers $headers
if ($getResponse) {
    Write-Host "✓ Successfully retrieved request details"
    Write-Host "Current Status: $($getResponse.status)"
} else {
    Write-Host "✗ Failed to fetch request details"
}

Write-Host "`n=== Test Summary ==="
Write-Host "Request ID tested: $requestId"
Write-Host "User ID used: $userId"
Write-Host "Completed all API tests"