# API Gateway Event Tests (PowerShell)
# Usage: Open PowerShell, cd to repo root, then: `.	ools\api-gateway-event-tests.ps1`
# Set $BASE_URL to your API Gateway base (default http://localhost:9090).

param(
    [string]$BaseUrl = "http://localhost:9090",
    [string]$AuthHeader = "",  # If your gateway requires Authorization header, pass like 'Bearer <token>'
    [string]$FirebaseUid = "cust-123"  # Firebase UID of the authenticated user
)

function Invoke-Api {
    param(
        [string]$Method = 'GET',
        [string]$Path,
        $Body = $null
    )

    $uri = "$BaseUrl$Path"
    Write-Host "=> $Method $uri"

    $headers = @{}
    if ($AuthHeader -ne "") { $headers['Authorization'] = $AuthHeader }
    $headers['Content-Type'] = 'application/json'

    try {
        if ($Body -ne $null) {
            $json = $Body | ConvertTo-Json -Depth 10
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -Body $json -ErrorAction Stop
        }
        else {
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ErrorAction Stop
        }
        return $resp
    }
    catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $reader.ReadToEnd() | Write-Host
        }
        return $null
    }
}

# Example sequence. Adjust IDs and payloads as needed.

# 1) Appointment: Create booking
$createBooking = @{ serviceId = 1; timeSlotId = 1; userId = $FirebaseUid; customerName = 'Jane Customer'; customerEmail = 'jane@example.com'; customerPhone = '555-0100'; notes = 'Test booking via curl tests' }
$created = Invoke-Api -Method POST -Path '/api/v1/bookings' -Body $createBooking
Write-Host "Created booking response:`n" ($created | ConvertTo-Json -Depth 6)

$bookingId = $null
if ($created -ne $null) {
    if ($created.id) { $bookingId = $created.id }
    elseif ($created.bookingId) { $bookingId = $created.bookingId }
}

if ($bookingId) {
    Write-Host "Extracted booking id: $bookingId"
    # 2) Approve booking
    Invoke-Api -Method PUT -Path "/api/v1/bookings/$bookingId/approve"

    # 3) Assign employee (this triggers EmployeeAssignedToAppointmentEvent)
    Invoke-Api -Method PUT -Path "/api/v1/bookings/$bookingId/assign?employeeId=101&timeSlot=09:00-10:00"
}

# 5) Payment events: create payment request (admin endpoint - skipped, requires admin role)
# $paymentReq = @{
#     customerName  = 'Jane Customer';
#     customerEmail = 'jane@example.com';
#     vehicleInfo   = 'Toyota Corolla 2021';
#     services      = @(
#         @{ code = 'SVC-1'; description = 'Oil change'; price = 49.99 },
#         @{ code = 'SVC-2'; description = 'Tire rotation'; price = 29.99 }
#     );
#     submittedBy   = 'system-tests';
#     submittedDate = (Get-Date).ToString('yyyy-MM-dd')
# }
# $paymentResp = Invoke-Api -Method POST -Path '/api/v1/payments/admin/requests' -Body $paymentReq
# Write-Host "Payment request response:`n" ($paymentResp | ConvertTo-Json -Depth 6)
Write-Host "Skipping admin payment request (requires admin role)" -ForegroundColor Yellow
$paymentId = $null

# 6) Customer: create and update KYC
$customerCreate = @{ firebaseUid = $FirebaseUid; email = 'jane@example.com'; displayName = 'Jane Customer'; phone = '555-0100' }
Invoke-Api -Method POST -Path '/api/v1/customers' -Body $customerCreate

# Update KYC
Invoke-Api -Method PATCH -Path "/api/v1/customers/$FirebaseUid/kyc?status=VERIFIED"

# 7) User registration and role assignment (skipped - user already registered by wrapper script)
# The wrapper script already registers the user, so we skip duplicate registration
# Admin role assignment also requires admin privileges
Write-Host "Skipping user registration (already done by wrapper) and role assignment (requires admin)" -ForegroundColor Yellow

# 8) Vehicle registration and update
$vehicleReq = @{ customerId = $FirebaseUid; make = 'Toyota'; model = 'Corolla'; year = 2021 }
Invoke-Api -Method POST -Path '/api/v1/vehicles' -Body $vehicleReq
Invoke-Api -Method PUT -Path '/api/v1/vehicles/1' -Body @{ updatedField = 'color'; value = 'Blue' }

# 9) Modification requests
$modReq = @{ customerId = $FirebaseUid; serviceId = 1; description = 'Add sunroof'; estimatedCost = 250.00 }
$modResp = Invoke-Api -Method POST -Path '/api/v1/service-modifications/1/requests' -Body $modReq
Write-Host "Modification create response:`n$modResp"

# 10) Parts request (creates parts request which may trigger events)
$partsReq = @{ partId = 999; partName = 'Brake Pad'; quantity = 2; reason = 'Needed for job' }
Invoke-Api -Method POST -Path '/api/v1/parts-requests' -Body $partsReq

# 11) Chatbot: start a session (send message) and close
# Generate unique session ID
$sessionId = "session-" + [guid]::NewGuid().ToString()
$chatMsg = @{ content = 'Hello, I want to start a session'; sessionId = $sessionId; userId = $FirebaseUid }
$chatResp = Invoke-Api -Method POST -Path '/api/v1/chat/send' -Body $chatMsg
Write-Host "Chat send response:`n" ($chatResp | ConvertTo-Json -Depth 6)
if ($chatResp -ne $null -and $chatResp.sessionId) { $sessionId = $chatResp.sessionId }
if ($sessionId) { Invoke-Api -Method POST -Path "/api/v1/chat/sessions/$sessionId/close"; Write-Host "Closed chat session $sessionId" }

# 12) Tracking tasks: create and update task
$taskReq = @{ title = 'Inspect vehicle'; description = 'Inspect brakes'; assigneeId = 'emp-101'; vehicleId = 'veh-1' }
Invoke-Api -Method POST -Path '/api/v1/tracking/tasks' -Body $taskReq

Write-Host "Tests complete. Check notification service logs or DB to confirm notifications were created." -ForegroundColor Green
