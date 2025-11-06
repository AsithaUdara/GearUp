# Comprehensive Service Test
# Tests all running microservices

$ErrorActionPreference = "Continue"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "GearUp Microservices Status Check" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Test User Auth Service
Write-Host "[1] User Auth Service (port 8082)" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -TimeoutSec 5
    Write-Host "  ✅ Status: $($health.status)" -ForegroundColor Green
    Write-Host "  📍 URL: http://localhost:8082" -ForegroundColor White
    $userAuthUp = $true
} catch {
    Write-Host "  ❌ Not Running" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    $userAuthUp = $false
}

# Test Eureka Server
Write-Host ""
Write-Host "[2] Eureka Server (port 8761)" -ForegroundColor Yellow
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761" -TimeoutSec 5 -UseBasicParsing
    Write-Host "  ✅ Status: Running" -ForegroundColor Green
    Write-Host "  📍 Dashboard: http://localhost:8761" -ForegroundColor White
    $eurekaUp = $true
} catch {
    Write-Host "  ❌ Not Running" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    $eurekaUp = $false
}

# Test API Gateway
Write-Host ""
Write-Host "[3] API Gateway (port 8080)" -ForegroundColor Yellow
try {
    $gateway = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "  ✅ Status: $($gateway.status)" -ForegroundColor Green
    Write-Host "  📍 URL: http://localhost:8080" -ForegroundColor White
    $gatewayUp = $true
} catch {
    Write-Host "  ❌ Not Running" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
    $gatewayUp = $false
}

# Test Database
Write-Host ""
Write-Host "[4] PostgreSQL Database (port 5434)" -ForegroundColor Yellow
try {
    $env:PGPASSWORD='Niro'
    $dbTest = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "SELECT COUNT(*) FROM users;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Status: Connected" -ForegroundColor Green
        Write-Host "  📍 Host: localhost:5434" -ForegroundColor White
        $dbUp = $true
    } else {
        Write-Host "  ❌ Connection Failed" -ForegroundColor Red
        $dbUp = $false
    }
} catch {
    Write-Host "  ❌ Not Accessible" -ForegroundColor Red
    $dbUp = $false
}

# Docker Services
Write-Host ""
Write-Host "[5] Docker Services" -ForegroundColor Yellow
try {
    $dockerPs = docker ps --format "{{.Names}}" 2>&1
    if ($dockerPs -match "redis") {
        Write-Host "  ✅ Redis: Running" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Redis: Not Running" -ForegroundColor Yellow
    }
    if ($dockerPs -match "rabbitmq") {
        Write-Host "  ✅ RabbitMQ: Running" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  RabbitMQ: Not Running" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Docker not accessible" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$totalServices = 0
$runningServices = 0

if ($userAuthUp) { $runningServices++; $totalServices++ } else { $totalServices++ }
if ($eurekaUp) { $runningServices++; $totalServices++ } else { $totalServices++ }
if ($gatewayUp) { $runningServices++; $totalServices++ } else { $totalServices++ }
if ($dbUp) { $runningServices++; $totalServices++ } else { $totalServices++ }

Write-Host "Services Running: $runningServices/$totalServices" -ForegroundColor $(if ($runningServices -eq $totalServices) { "Green" } else { "Yellow" })
Write-Host ""

if ($userAuthUp) {
    Write-Host "✅ Ready to test user authentication!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Test user registration:" -ForegroundColor White
    Write-Host "   POST http://localhost:8082/api/v1/users/register" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Test user login:" -ForegroundColor White
    Write-Host "   POST http://localhost:8082/api/v1/auth/login" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Access via Gateway:" -ForegroundColor White
    if ($gatewayUp) {
        Write-Host "   http://localhost:8080/user-auth-service/api/v1/..." -ForegroundColor Gray
    } else {
        Write-Host "   (Gateway not running - use direct service URL)" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "4. View Eureka Dashboard:" -ForegroundColor White
    if ($eurekaUp) {
        Write-Host "   http://localhost:8761" -ForegroundColor Gray
    } else {
        Write-Host "   (Eureka not running yet)" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠️  User Auth Service is not running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "To start it:" -ForegroundColor Yellow
    Write-Host "  cd 'c:\Users\ASUS\Desktop\Final EAD\GearUp-backend'" -ForegroundColor White
    Write-Host "  .\mvnw spring-boot:run -pl services/user-auth-service" -ForegroundColor White
}

Write-Host ""
