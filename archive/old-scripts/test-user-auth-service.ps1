# Test User Auth Service
# Quick test script for user-auth-service

$ErrorActionPreference = "Stop"
$BaseUrl = "http://localhost:8082"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "User Auth Service - Quick Test" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "Test 1: Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET
    Write-Host "  ✅ Service is UP" -ForegroundColor Green
    Write-Host "  Status: $($health.status)" -ForegroundColor White
} catch {
    Write-Host "  ❌ Service is not running or not accessible" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "To start the service, run:" -ForegroundColor Yellow
    Write-Host "  cd 'c:\Users\ASUS\Desktop\Final EAD\GearUp-backend'" -ForegroundColor White
    Write-Host "  .\mvnw spring-boot:run -pl services/user-auth-service" -ForegroundColor White
    exit 1
}

# Test 2: Database connectivity (via actuator if available)
Write-Host ""
Write-Host "Test 2: Service Info..." -ForegroundColor Yellow
try {
    $info = Invoke-RestMethod -Uri "$BaseUrl/actuator/info" -Method GET -ErrorAction SilentlyContinue
    if ($info) {
        Write-Host "  ✅ Info endpoint accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "  Info endpoint not configured" -ForegroundColor Gray
}

# Test 3: Register a test user
Write-Host ""
Write-Host "Test 3: Register Test User..." -ForegroundColor Yellow
$testUser = @{
    firebaseUid = "test-uid-$(Get-Random)"
    email = "test-$(Get-Random)@gearup.test"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "+1234567890"
    displayName = "Test User"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/users/register" -Method POST -Body $testUser -ContentType "application/json"
    Write-Host "  ✅ User registration successful" -ForegroundColor Green
    Write-Host "  User ID: $($registerResponse.id)" -ForegroundColor White
    Write-Host "  Email: $($registerResponse.email)" -ForegroundColor White
} catch {
    Write-Host "  ⚠️  Registration failed (endpoint may need authentication)" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}

# Test 4: Login with default admin (if exists)
Write-Host ""
Write-Host "Test 4: Login with Default Admin..." -ForegroundColor Yellow
$loginData = @{
    email = "admin@gearup.com"
    password = "Admin@123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "  ✅ Login successful" -ForegroundColor Green
    Write-Host "  Access Token: $($loginResponse.accessToken.Substring(0, 50))..." -ForegroundColor White
} catch {
    Write-Host "  ℹ️  Login failed (admin user may not exist yet)" -ForegroundColor Gray
    Write-Host "  This is okay for a new installation" -ForegroundColor Gray
}

# Summary
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Service is running on port 8082" -ForegroundColor Green
Write-Host "✅ Health check passed" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Check database for created users:" -ForegroundColor White
Write-Host "   psql -U postgres -h localhost -p 5434 -d as_user_auth_service" -ForegroundColor Gray
Write-Host "   SELECT * FROM users;" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Test with your frontend:" -ForegroundColor White
Write-Host "   Configure frontend to connect to: $BaseUrl" -ForegroundColor Gray
Write-Host ""
Write-Host "3. View full API documentation:" -ForegroundColor White
Write-Host "   See: docs/USER_AUTH_SETUP_COMPLETE.md" -ForegroundColor Gray
Write-Host ""
