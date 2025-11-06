# GearUp Microservices Startup Script
# Usage: Run this script to start all backend services
# 
# Prerequisites:
#   1. Set environment variables (run: . .\SET_ENV_VARS.ps1)
#   2. Ensure PostgreSQL is running on port 5434
#   3. Ensure Docker services (Redis, RabbitMQ) are running

$ErrorActionPreference = "Continue"

# Dynamically determine backend root (script's parent directory)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendRoot = Split-Path -Parent $ScriptDir

Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "║        GearUp Microservices Startup Script           ║" -ForegroundColor Yellow
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend Root: $BackendRoot" -ForegroundColor Gray
Write-Host ""

# Check environment variables
Write-Host "[0/6] Checking environment variables..." -ForegroundColor Yellow
$missingVars = @()
if (-not $env:POSTGRES_PASSWORD) {
    $missingVars += "POSTGRES_PASSWORD"
}
if ($missingVars.Count -gt 0) {
    Write-Host "  ⚠️  Missing environment variables: $($missingVars -join ', ')" -ForegroundColor Red
    Write-Host "  💡 Run this first: . .\SET_ENV_VARS.ps1" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "  ✅ Environment variables are set" -ForegroundColor Green
}

# Check PostgreSQL
Write-Host ""
Write-Host "[1/6] Checking PostgreSQL..." -ForegroundColor Yellow
try {
    # Use environment variable if set
    $pgPassword = if ($env:POSTGRES_PASSWORD) { $env:POSTGRES_PASSWORD } else { 'postgres' }
    $env:PGPASSWORD = $pgPassword
    
    $pgTest = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -p 5434 -l -t 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ PostgreSQL is running on port 5434" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  PostgreSQL not accessible. Please start it manually." -ForegroundColor Red
        Write-Host "  💡 Set POSTGRES_PASSWORD environment variable" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Could not verify PostgreSQL status" -ForegroundColor Red
    Write-Host "  💡 Ensure PostgreSQL 18 is installed at: C:\Program Files\PostgreSQL\18" -ForegroundColor Yellow
}

# Check Docker services
Write-Host ""
Write-Host "[2/6] Checking Docker services..." -ForegroundColor Yellow
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "redis|rabbitmq"
Write-Host ""

# Start Eureka Server
Write-Host ""
Write-Host "[3/6] Starting Eureka Server (Service Discovery)..." -ForegroundColor Yellow
Set-Location $BackendRoot
$eurekaCmd = "cd '$BackendRoot'; `$env:POSTGRES_PASSWORD='$env:POSTGRES_PASSWORD'; .\mvnw spring-boot:run -pl service-discovery"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $eurekaCmd
Write-Host "  🚀 Eureka Server starting in new window (port 8761)..." -ForegroundColor Green
Write-Host "  📍 http://localhost:8761" -ForegroundColor Gray
Start-Sleep -Seconds 5

# Start Config Server (Optional - currently disabled in services)
Write-Host ""
Write-Host "[4/6] Starting Config Server..." -ForegroundColor Yellow
$configCmd = "cd '$BackendRoot'; `$env:POSTGRES_PASSWORD='$env:POSTGRES_PASSWORD'; .\mvnw spring-boot:run -pl config-server"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $configCmd
Write-Host "  🚀 Config Server starting in new window (port 8888)..." -ForegroundColor Green
Write-Host "  📍 http://localhost:8888" -ForegroundColor Gray
Write-Host "  💡 Note: Config Server is currently disabled in services" -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Start User Auth Service
Write-Host ""
Write-Host "[5/6] Starting User Auth Service..." -ForegroundColor Yellow
$userAuthCmd = "cd '$BackendRoot'; `$env:POSTGRES_PASSWORD='$env:POSTGRES_PASSWORD'; `$env:RABBITMQ_PASSWORD='$env:RABBITMQ_PASSWORD'; .\mvnw spring-boot:run -pl services/user-auth-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $userAuthCmd
Write-Host "  🚀 User Auth Service starting in new window (port 8082)..." -ForegroundColor Green
Write-Host "  📍 http://localhost:8082" -ForegroundColor Gray
Start-Sleep -Seconds 5

# Start API Gateway
Write-Host ""
Write-Host "[6/6] Starting API Gateway..." -ForegroundColor Yellow
$gatewayCmd = "cd '$BackendRoot'; `$env:POSTGRES_PASSWORD='$env:POSTGRES_PASSWORD'; .\mvnw spring-boot:run -pl api-gateway"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $gatewayCmd
Write-Host "  🚀 API Gateway starting in new window (port 8080)..." -ForegroundColor Green
Write-Host "  📍 http://localhost:8080" -ForegroundColor Gray

Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "║          ✅ All Services Are Starting!                ║" -ForegroundColor Green
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎯 Services will be available at:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Service Discovery:" -ForegroundColor Cyan
Write-Host "    Eureka Dashboard    → http://localhost:8761" -ForegroundColor White
Write-Host ""
Write-Host "  Gateway & Config:" -ForegroundColor Cyan
Write-Host "    API Gateway         → http://localhost:8080" -ForegroundColor White
Write-Host "    Config Server       → http://localhost:8888" -ForegroundColor White
Write-Host ""
Write-Host "  Microservices:" -ForegroundColor Cyan
Write-Host "    User Auth Service   → http://localhost:8082" -ForegroundColor White
Write-Host ""
Write-Host "  Supporting Services:" -ForegroundColor Cyan
Write-Host "    PostgreSQL          → localhost:5434" -ForegroundColor White
Write-Host "    Redis               → localhost:6379" -ForegroundColor White
Write-Host "    RabbitMQ Admin      → http://localhost:15672" -ForegroundColor White
Write-Host ""
Write-Host "🔍 Health Checks:" -ForegroundColor Yellow
Write-Host "  Invoke-RestMethod http://localhost:8082/actuator/health  # User Auth" -ForegroundColor Gray
Write-Host "  Invoke-RestMethod http://localhost:8080/actuator/health  # API Gateway" -ForegroundColor Gray
Write-Host ""
Write-Host "📝 Verify All Services:" -ForegroundColor Yellow
Write-Host "  .\scripts\check-all-services.ps1" -ForegroundColor Gray
Write-Host ""
Write-Host "⏱️  Please wait 30-60 seconds for all services to start..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Press any key to open Eureka Dashboard..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process "http://localhost:8761"

Write-Host ""
Write-Host "✅ Startup script completed!" -ForegroundColor Green
Write-Host "📺 Check the individual PowerShell windows for service logs." -ForegroundColor White
Write-Host "💡 Tip: Close any window to stop that service." -ForegroundColor Yellow
Write-Host ""
