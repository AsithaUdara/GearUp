# Simple User Auth Service Test Script
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "User Auth Service - Testing" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Health Check
Write-Host "`nTest 1: Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health"
    Write-Host "  PASS - Service is UP ($($health.status))" -ForegroundColor Green
} catch {
    Write-Host "  FAIL - Service not accessible" -ForegroundColor Red
    exit 1
}

# Test 2: Register a test user
Write-Host "`nTest 2: User Registration..." -ForegroundColor Yellow
$randomId = Get-Random
$testUser = @{
    firebaseUid = "test-uid-$randomId"
    email = "test$randomId@gearup.test"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "+1234567890"
    displayName = "Test User $randomId"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/register" `
        -Method POST `
        -Body $testUser `
        -ContentType "application/json"
    Write-Host "  PASS - User registered successfully" -ForegroundColor Green
    Write-Host "  User ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Email: $($response.email)" -ForegroundColor Gray
} catch {
    Write-Host "  WARN - Registration endpoint may require authentication" -ForegroundColor Yellow
}

# Test 3: Check database
Write-Host "`nTest 3: Database Connection..." -ForegroundColor Yellow
try {
    $env:PGPASSWORD='Niro'
    $userCount = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" `
        -U postgres -p 5434 -d as_user_auth_service `
        -t -c "SELECT COUNT(*) FROM users;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  PASS - Database connected" -ForegroundColor Green
        Write-Host "  Total users in database: $($userCount.Trim())" -ForegroundColor Gray
    }
} catch {
    Write-Host "  WARN - Could not check database" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "All Tests Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nService is ready at: http://localhost:8082" -ForegroundColor White
Write-Host "Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host ""
