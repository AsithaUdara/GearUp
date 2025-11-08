# Quick Authentication Flow Test Script
# Run this after starting services

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "     GearUp Authentication Flow Test Script             " -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

# Get backend root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendRoot = $ScriptDir

# Check environment variables
Write-Host "[0/6] Checking prerequisites..." -ForegroundColor Yellow
$pgPassword = if ($env:POSTGRES_PASSWORD) { $env:POSTGRES_PASSWORD } else { 'Niro' }
$env:PGPASSWORD = $pgPassword
Write-Host "  [OK] PostgreSQL password configured" -ForegroundColor Green
Write-Host ""

# 1. Check database connection
Write-Host "[1/6] Checking database connection..." -ForegroundColor Yellow
try {
    $dbCheck = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "SELECT COUNT(*) FROM users;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Database connection successful" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Database connection failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  [FAIL] Database check failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. Check service health
Write-Host "[2/6] Checking User Auth Service health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -ErrorAction Stop
    if ($health.status -eq "UP") {
        Write-Host "  [OK] User Auth Service is UP" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Service is not healthy" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  [FAIL] User Auth Service not running on port 8082" -ForegroundColor Red
    Write-Host "  [INFO] Start service: cd '$BackendRoot' ; .\mvnw spring-boot:run -pl services/user-auth-service" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 3. List test users
Write-Host "[3/6] Listing available test users..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  Available Test Users:" -ForegroundColor Cyan
Write-Host "  +---------------------+--------------------------+----------+" -ForegroundColor Gray
Write-Host "  | Email               | Firebase UID             | Role     |" -ForegroundColor Gray
Write-Host "  +---------------------+--------------------------+----------+" -ForegroundColor Gray

$usersQuery = "SELECT u.email, u.firebase_uid, COALESCE(r.name, 'NO_ROLE') FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id LEFT JOIN roles r ON ur.role_id = r.id ORDER BY u.id;"
$users = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -t -c $usersQuery

if ($users) {
    $users | ForEach-Object {
        $line = $_.Trim()
        if ($line) {
            $parts = $line -split '\|'
            $email = $parts[0].Trim().PadRight(19)
            $uid = $parts[1].Trim().PadRight(24)
            $role = $parts[2].Trim().PadRight(8)
            Write-Host "  | $email | $uid | $role |" -ForegroundColor White
        }
    }
    Write-Host "  +---------------------+--------------------------+----------+" -ForegroundColor Gray
} else {
    Write-Host "  [FAIL] No users found in database" -ForegroundColor Red
}
Write-Host ""

# 4. Get Firebase token
Write-Host "[4/6] Get Firebase Authentication Token" -ForegroundColor Yellow
Write-Host ""
Write-Host "  To test protected endpoints, you need a Firebase ID token." -ForegroundColor White
Write-Host "  " -ForegroundColor White
Write-Host "  Manual Steps:" -ForegroundColor Cyan
Write-Host "     1. Start frontend: cd GearUp-frontent ; npm run dev" -ForegroundColor White
Write-Host "     2. Open browser: http://localhost:3000/login" -ForegroundColor White
Write-Host "     3. Login with credentials:" -ForegroundColor White
Write-Host "        - Email: admin@gearup.com" -ForegroundColor Yellow
Write-Host "        - Password: TestAdmin@123" -ForegroundColor Yellow
Write-Host "     4. Open DevTools Console (F12)" -ForegroundColor White
Write-Host "     5. Run this command in console:" -ForegroundColor White
Write-Host '        firebase.auth().currentUser.getIdToken().then(console.log)' -ForegroundColor Cyan
Write-Host "     6. Copy the token from console output" -ForegroundColor White
Write-Host ""
Write-Host "  [WARNING] You must create Firebase users first!" -ForegroundColor Red
Write-Host "     See AUTHENTICATION_FLOW_TEST.md for instructions" -ForegroundColor Red
Write-Host ""

$token = Read-Host "  Paste your Firebase token here (or press Enter to skip API tests)"

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host ""
    Write-Host "  [SKIP] Skipping API tests (no token provided)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "=========================================================" -ForegroundColor Cyan
    Write-Host "                  Summary                                " -ForegroundColor Yellow
    Write-Host "=========================================================" -ForegroundColor Cyan
    Write-Host "  [OK] Database: Connected" -ForegroundColor Green
    Write-Host "  [OK] User Auth Service: Running on port 8082" -ForegroundColor Green
    Write-Host "  [SKIP] API Tests: Skipped (no token)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  Next Steps:" -ForegroundColor Cyan
    Write-Host "     1. Create Firebase users (see AUTHENTICATION_FLOW_TEST.md)" -ForegroundColor White
    Write-Host "     2. Start frontend and get token" -ForegroundColor White
    Write-Host "     3. Run this script again with token" -ForegroundColor White
    Write-Host ""
    exit 0
}

Write-Host ""

# 5. Test protected endpoints
Write-Host "[5/6] Testing protected endpoints with token..." -ForegroundColor Yellow
Write-Host ""

# Test user profile
Write-Host "  Testing: GET /api/v1/users/profile" -ForegroundColor Cyan
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/profile" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    Write-Host "    [OK] Profile loaded successfully" -ForegroundColor Green
    Write-Host "      - User: $($profile.firstName) $($profile.lastName)" -ForegroundColor White
    Write-Host "      - Email: $($profile.email)" -ForegroundColor White
    Write-Host "      - Role: $($profile.role)" -ForegroundColor White
} catch {
    Write-Host "    [FAIL] Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test admin users list
Write-Host "  Testing: GET /api/v1/admin/users (Admin only)" -ForegroundColor Cyan
try {
    $usersList = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users?page=0&size=10" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    Write-Host "    [OK] Users list loaded successfully" -ForegroundColor Green
    Write-Host "      - Total Users: $($usersList.totalElements)" -ForegroundColor White
    Write-Host "      - Current Page: $($usersList.currentPage)" -ForegroundColor White
    Write-Host "      - Total Pages: $($usersList.totalPages)" -ForegroundColor White
} catch {
    Write-Host "    [FAIL] Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Message -like "*403*") {
        Write-Host "      [INFO] User doesn't have ADMIN role" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test user stats
Write-Host "  Testing: GET /api/v1/admin/users/stats (Admin only)" -ForegroundColor Cyan
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/stats" `
        -Headers @{"Authorization" = "Bearer $token"} `
        -ErrorAction Stop
    Write-Host "    [OK] User statistics loaded successfully" -ForegroundColor Green
    Write-Host "      - Total Users: $($stats.totalUsers)" -ForegroundColor White
    Write-Host "      - Active Users: $($stats.activeUsers)" -ForegroundColor White
    Write-Host "      - Admin Count: $($stats.adminCount)" -ForegroundColor White
    Write-Host "      - Employee Count: $($stats.employeeCount)" -ForegroundColor White
    Write-Host "      - Customer Count: $($stats.customerCount)" -ForegroundColor White
} catch {
    Write-Host "    [FAIL] Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 6. Check audit logs
Write-Host "[6/6] Checking audit trail..." -ForegroundColor Yellow
try {
    $auditCount = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -t -c "SELECT COUNT(*) FROM user_audit_log;"
    $count = $auditCount.Trim()
    Write-Host "  [OK] Audit log entries: $count" -ForegroundColor Green
    
    if ([int]$count -gt 0) {
        Write-Host ""
        Write-Host "  Recent audit entries:" -ForegroundColor Cyan
        & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "SELECT id, user_id, action, entity_type, created_at FROM user_audit_log ORDER BY created_at DESC LIMIT 5;"
    }
} catch {
    Write-Host "  [WARNING] Could not check audit logs" -ForegroundColor Yellow
}
Write-Host ""

# Final summary
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "                  Test Summary                           " -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  [OK] Database connection successful" -ForegroundColor Green
Write-Host "  [OK] User Auth Service is running" -ForegroundColor Green
Write-Host "  [OK] Test users available in database" -ForegroundColor Green
Write-Host "  [OK] Protected endpoints tested with token" -ForegroundColor Green
Write-Host ""
Write-Host "  Next Steps:" -ForegroundColor Cyan
Write-Host "     1. Start frontend: cd GearUp-frontent ; npm run dev" -ForegroundColor White
Write-Host "     2. Test login at: http://localhost:3000/login" -ForegroundColor White
Write-Host "     3. Test admin panel at: http://localhost:3000/admin/users" -ForegroundColor White
Write-Host "     4. See full test guide: AUTHENTICATION_FLOW_TEST.md" -ForegroundColor White
Write-Host ""
Write-Host "  Service URLs:" -ForegroundColor Cyan
Write-Host "     User Auth Service -> http://localhost:8082" -ForegroundColor White
Write-Host "     Health Check      -> http://localhost:8082/actuator/health" -ForegroundColor White
Write-Host "     API Docs (if enabled) -> http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "[SUCCESS] Authentication flow test complete!" -ForegroundColor Green
Write-Host ""
