# Test Admin User Management API
# This script tests all admin endpoints
# 
# REQUIREMENTS:
# 1. User Auth Service must be running on port 8082
# 2. You must have a Firebase ID token from an ADMIN user
# 3. Replace $token variable below with your actual Firebase token
#
# HOW TO GET TOKEN:
# 1. Login as admin in frontend
# 2. Open browser console
# 3. Run: firebase.auth().currentUser.getIdToken().then(t => console.log(t))
# 4. Copy the token and paste it below

Write-Host "`n╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                           ║" -ForegroundColor Cyan
Write-Host "║      ADMIN USER MANAGEMENT API - TEST SCRIPT              ║" -ForegroundColor Green
Write-Host "║                                                           ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ============================================
# CONFIGURATION
# ============================================
$baseUrl = "http://localhost:8082/api/v1/admin/users"
$token = "YOUR_FIREBASE_TOKEN_HERE"  # ⚠️ REPLACE THIS WITH YOUR ACTUAL ADMIN TOKEN

# Check if token is set
if ($token -eq "YOUR_FIREBASE_TOKEN_HERE") {
    Write-Host "⚠️  ERROR: Please set your Firebase ID token!" -ForegroundColor Red
    Write-Host ""
    Write-Host "How to get your token:" -ForegroundColor Yellow
    Write-Host "1. Login as ADMIN in your frontend" -ForegroundColor White
    Write-Host "2. Open browser console (F12)" -ForegroundColor White
    Write-Host "3. Run: firebase.auth().currentUser.getIdToken().then(t => console.log(t))" -ForegroundColor Gray
    Write-Host "4. Copy the token and paste it in this script" -ForegroundColor White
    Write-Host ""
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# ============================================
# TEST 1: Get All Users
# ============================================
Write-Host "`n[TEST 1] Get All Users (Paginated)" -ForegroundColor Yellow
Write-Host "GET $baseUrl?page=0&size=10" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl?page=0&size=10" -Headers $headers -Method GET
    Write-Host "✅ SUCCESS" -ForegroundColor Green
    Write-Host "Total Users: $($response.data.totalElements)" -ForegroundColor White
    Write-Host "Users in page: $($response.data.content.Count)" -ForegroundColor White
    if ($response.data.content.Count -gt 0) {
        Write-Host "`nFirst user:" -ForegroundColor Cyan
        $response.data.content[0] | Format-List
    }
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================
# TEST 2: Search Users
# ============================================
Write-Host "`n[TEST 2] Search Users" -ForegroundColor Yellow
Write-Host "GET $baseUrl?search=admin" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl?search=admin" -Headers $headers -Method GET
    Write-Host "✅ SUCCESS" -ForegroundColor Green
    Write-Host "Found: $($response.data.totalElements) users" -ForegroundColor White
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================
# TEST 3: Filter by Role
# ============================================
Write-Host "`n[TEST 3] Filter Users by Role (ADMIN)" -ForegroundColor Yellow
Write-Host "GET $baseUrl?role=ADMIN" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl?role=ADMIN" -Headers $headers -Method GET
    Write-Host "✅ SUCCESS" -ForegroundColor Green
    Write-Host "Admin users: $($response.data.totalElements)" -ForegroundColor White
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================
# TEST 4: Get User Statistics
# ============================================
Write-Host "`n[TEST 4] Get User Statistics" -ForegroundColor Yellow
Write-Host "GET $baseUrl/stats" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/stats" -Headers $headers -Method GET
    Write-Host "✅ SUCCESS" -ForegroundColor Green
    Write-Host "`nStatistics:" -ForegroundColor Cyan
    Write-Host "  Total Users: $($response.data.totalUsers)" -ForegroundColor White
    Write-Host "  Active: $($response.data.activeUsers)" -ForegroundColor Green
    Write-Host "  Deactivated: $($response.data.deactivatedUsers)" -ForegroundColor Red
    Write-Host "  Customers: $($response.data.totalCustomers)" -ForegroundColor White
    Write-Host "  Employees: $($response.data.totalEmployees)" -ForegroundColor White
    Write-Host "  Admins: $($response.data.totalAdmins)" -ForegroundColor White
    Write-Host "  New This Month: $($response.data.newUsersThisMonth)" -ForegroundColor Yellow
    Write-Host "  New Today: $($response.data.newUsersToday)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ============================================
# TEST 5: Create Employee
# ============================================
Write-Host "`n[TEST 5] Create New Employee" -ForegroundColor Yellow
$newEmployee = @{
    email = "test.employee.$(Get-Date -Format 'yyyyMMddHHmmss')@gearup.com"
    name = "Test Employee $(Get-Date -Format 'HHmmss')"
    role = "EMPLOYEE"
    phoneNumber = "+1234567890"
} | ConvertTo-Json

Write-Host "POST $baseUrl/employees" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/employees" -Headers $headers -Method POST -Body $newEmployee
    Write-Host "✅ SUCCESS" -ForegroundColor Green
    Write-Host "Created User ID: $($response.data.id)" -ForegroundColor White
    Write-Host "Email: $($response.data.email)" -ForegroundColor White
    Write-Host "Role: $($response.data.roles[0].name)" -ForegroundColor White
    
    # Save the created user ID for next tests
    $createdUserId = $response.data.id
} catch {
    Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    $createdUserId = $null
}

# ============================================
# TEST 6: Get User by ID
# ============================================
if ($createdUserId) {
    Write-Host "`n[TEST 6] Get User by ID" -ForegroundColor Yellow
    Write-Host "GET $baseUrl/$createdUserId" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$createdUserId" -Headers $headers -Method GET
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host "`nUser Details:" -ForegroundColor Cyan
        $response.data | Format-List
    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }

    # ============================================
    # TEST 7: Update User
    # ============================================
    Write-Host "`n[TEST 7] Update User (Change Role to ADMIN)" -ForegroundColor Yellow
    $updateData = @{
        role = "ADMIN"
        status = "Active"
    } | ConvertTo-Json

    Write-Host "PUT $baseUrl/$createdUserId" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$createdUserId" -Headers $headers -Method PUT -Body $updateData
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host "New Role: $($response.data.roles[0].name)" -ForegroundColor White
        Write-Host "Status: $($response.data.accountStatus)" -ForegroundColor White
    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }

    # ============================================
    # TEST 8: Deactivate User
    # ============================================
    Write-Host "`n[TEST 8] Deactivate User" -ForegroundColor Yellow
    Write-Host "PATCH $baseUrl/$createdUserId/deactivate" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$createdUserId/deactivate" -Headers $headers -Method PATCH
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host "Status: $($response.data.accountStatus)" -ForegroundColor Red
    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }

    # ============================================
    # TEST 9: Activate User
    # ============================================
    Write-Host "`n[TEST 9] Activate User" -ForegroundColor Yellow
    Write-Host "PATCH $baseUrl/$createdUserId/activate" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$createdUserId/activate" -Headers $headers -Method PATCH
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host "Status: $($response.data.accountStatus)" -ForegroundColor Green
    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }

    # ============================================
    # TEST 10: Delete User
    # ============================================
    Write-Host "`n[TEST 10] Delete User (Soft Delete)" -ForegroundColor Yellow
    Write-Host "DELETE $baseUrl/$createdUserId" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$createdUserId" -Headers $headers -Method DELETE
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host "User deleted (deactivated)" -ForegroundColor White
    } catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# ============================================
# SUMMARY
# ============================================
Write-Host "`n╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                           ║" -ForegroundColor Cyan
Write-Host "║                  TEST COMPLETE!                           ║" -ForegroundColor Green
Write-Host "║                                                           ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "All admin endpoints have been tested!" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Check the test results above" -ForegroundColor White
Write-Host "2. Integrate the APIs with your frontend" -ForegroundColor White
Write-Host "3. See docs/ADMIN_USER_MANAGEMENT_API.md for integration examples" -ForegroundColor White
Write-Host ""
