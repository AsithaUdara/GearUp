# Test Backend API with Firebase Token
# Run this after getting token from frontend

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "    Testing Backend API with Firebase Token             " -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

# Get token from user
Write-Host "Instructions:" -ForegroundColor Yellow
Write-Host "1. Login at localhost:3000 (modal popup)" -ForegroundColor White
Write-Host "2. Press F12 -> Console" -ForegroundColor White
Write-Host "3. Run: firebase.auth().currentUser.getIdToken().then(console.log)" -ForegroundColor Cyan
Write-Host "4. Copy the token" -ForegroundColor White
Write-Host ""

$token = Read-Host "Paste your Firebase token here"

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host ""
    Write-Host "[ERROR] No token provided!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "Starting API Tests..." -ForegroundColor Yellow
Write-Host ""

# Test 1: Get User Profile
Write-Host "[1/5] Testing: GET /api/v1/users/profile" -ForegroundColor Cyan
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/profile" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    
    Write-Host "  [SUCCESS] Profile loaded!" -ForegroundColor Green
    Write-Host "    User: $($profile.firstName) $($profile.lastName)" -ForegroundColor White
    Write-Host "    Email: $($profile.email)" -ForegroundColor White
    Write-Host "    Role: $($profile.role)" -ForegroundColor White
    Write-Host "    Status: $($profile.accountStatus)" -ForegroundColor White
} catch {
    Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    HTTP Status: $statusCode" -ForegroundColor Red
        if ($statusCode -eq 401) {
            Write-Host "    Possible issues:" -ForegroundColor Yellow
            Write-Host "      - Token expired (tokens expire after 1 hour)" -ForegroundColor Yellow
            Write-Host "      - Firebase UID doesn't match database" -ForegroundColor Yellow
            Write-Host "      - Backend Firebase config issue" -ForegroundColor Yellow
        }
    }
}
Write-Host ""

# Test 2: Get All Users (Admin only)
Write-Host "[2/5] Testing: GET /api/v1/admin/users (Admin Only)" -ForegroundColor Cyan
try {
    $usersList = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users?page=0&size=10" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    
    Write-Host "  [SUCCESS] Users list loaded!" -ForegroundColor Green
    Write-Host "    Total Users: $($usersList.totalElements)" -ForegroundColor White
    Write-Host "    Current Page: $($usersList.currentPage)" -ForegroundColor White
    Write-Host "    Total Pages: $($usersList.totalPages)" -ForegroundColor White
    
    if ($usersList.users -and $usersList.users.Count -gt 0) {
        Write-Host ""
        Write-Host "    Users:" -ForegroundColor Cyan
        $usersList.users | ForEach-Object {
            Write-Host "      - $($_.email) [$($_.role)] - $($_.accountStatus)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    HTTP Status: $statusCode" -ForegroundColor Red
        if ($statusCode -eq 403) {
            Write-Host "    Issue: User doesn't have ADMIN role!" -ForegroundColor Yellow
        }
    }
}
Write-Host ""

# Test 3: Get User Statistics (Admin only)
Write-Host "[3/5] Testing: GET /api/v1/admin/users/stats (Admin Only)" -ForegroundColor Cyan
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/stats" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    
    Write-Host "  [SUCCESS] Statistics loaded!" -ForegroundColor Green
    Write-Host "    Total Users: $($stats.totalUsers)" -ForegroundColor White
    Write-Host "    Active Users: $($stats.activeUsers)" -ForegroundColor White
    Write-Host "    Inactive Users: $($stats.inactiveUsers)" -ForegroundColor White
    Write-Host "    Admin Count: $($stats.adminCount)" -ForegroundColor White
    Write-Host "    Employee Count: $($stats.employeeCount)" -ForegroundColor White
    Write-Host "    Customer Count: $($stats.customerCount)" -ForegroundColor White
} catch {
    Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Create Employee (Admin only)
Write-Host "[4/5] Testing: POST /api/v1/admin/users/employee (Admin Only)" -ForegroundColor Cyan
$timestamp = Get-Date -Format "HHmmss"
$testEmployee = @{
    email = "testemployee$timestamp@gearup.com"
    password = "Test@123456"
    firstName = "Test"
    lastName = "Employee"
    phoneNumber = "+1234567890"
    role = "EMPLOYEE"
} | ConvertTo-Json

try {
    $newEmployee = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/employee" `
        -Method POST `
        -Headers @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        } `
        -Body $testEmployee `
        -ErrorAction Stop
    
    Write-Host "  [SUCCESS] Employee created!" -ForegroundColor Green
    Write-Host "    Email: $($newEmployee.email)" -ForegroundColor White
    Write-Host "    Name: $($newEmployee.firstName) $($newEmployee.lastName)" -ForegroundColor White
    Write-Host "    Role: $($newEmployee.role)" -ForegroundColor White
    
    $createdEmployeeId = $newEmployee.id
} catch {
    Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
    $createdEmployeeId = $null
}
Write-Host ""

# Test 5: Get User by ID (if employee was created)
if ($createdEmployeeId) {
    Write-Host "[5/5] Testing: GET /api/v1/admin/users/$createdEmployeeId (Admin Only)" -ForegroundColor Cyan
    try {
        $employeeDetails = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/$createdEmployeeId" `
            -Headers @{"Authorization" = "Bearer $token"} `
            -ErrorAction Stop
        
        Write-Host "  [SUCCESS] User details loaded!" -ForegroundColor Green
        Write-Host "    ID: $($employeeDetails.id)" -ForegroundColor White
        Write-Host "    Email: $($employeeDetails.email)" -ForegroundColor White
        Write-Host "    Role: $($employeeDetails.role)" -ForegroundColor White
        Write-Host "    Status: $($employeeDetails.accountStatus)" -ForegroundColor White
    } catch {
        Write-Host "  [FAILED] $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "[5/5] Skipping user details test (no employee created)" -ForegroundColor Yellow
}
Write-Host ""

# Summary
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "                  Test Summary                           " -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend Service: http://localhost:8082" -ForegroundColor White
Write-Host "Frontend: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "Next: Test the admin panel at /admin/users in frontend" -ForegroundColor Cyan
Write-Host ""
