# GearUp Microservices Startup Script
# Usage: Run this script to start all backend services

$ErrorActionPreference = "Continue"
$BackendRoot = "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "GearUp Microservices Startup" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check PostgreSQL
Write-Host "[1/6] Checking PostgreSQL..." -ForegroundColor Yellow
try {
    $pgTest = psql -U postgres -h localhost -p 5434 -l -t 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ PostgreSQL is running on port 5434" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  PostgreSQL not accessible. Please start it manually." -ForegroundColor Red
        Write-Host "     Password should be: Niro" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Could not verify PostgreSQL status" -ForegroundColor Red
}

# Check Docker services
Write-Host ""
Write-Host "[2/6] Checking Docker services..." -ForegroundColor Yellow
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "redis|rabbitmq"
Write-Host ""

# Start Eureka Server
Write-Host "[3/6] Starting Eureka Server (Service Discovery)..." -ForegroundColor Yellow
cd $BackendRoot
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BackendRoot'; .\mvnw spring-boot:run -pl service-discovery"
Write-Host "  🚀 Eureka Server starting in new window (port 8761)..." -ForegroundColor Green
Start-Sleep -Seconds 5

# Start Config Server
Write-Host ""
Write-Host "[4/6] Starting Config Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BackendRoot'; .\mvnw spring-boot:run -pl config-server"
Write-Host "  🚀 Config Server starting in new window (port 8888)..." -ForegroundColor Green
Start-Sleep -Seconds 5

# Start User Auth Service
Write-Host ""
Write-Host "[5/6] Starting User Auth Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BackendRoot'; .\mvnw spring-boot:run -pl services/user-auth-service"
Write-Host "  🚀 User Auth Service starting in new window (port 8082)..." -ForegroundColor Green
Start-Sleep -Seconds 5

# Start API Gateway
Write-Host ""
Write-Host "[6/6] Starting API Gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BackendRoot'; .\mvnw spring-boot:run -pl api-gateway"
Write-Host "  🚀 API Gateway starting in new window (port 8080)..." -ForegroundColor Green

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "✅ All services are starting!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services will be available at:" -ForegroundColor Yellow
Write-Host "  - Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  - User Auth Service: http://localhost:8082" -ForegroundColor White
Write-Host "  - Config Server: http://localhost:8888" -ForegroundColor White
Write-Host ""
Write-Host "Health Checks:" -ForegroundColor Yellow
Write-Host "  curl http://localhost:8082/actuator/health  # User Auth" -ForegroundColor White
Write-Host "  curl http://localhost:8080/actuator/health  # API Gateway" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to open Eureka Dashboard..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process "http://localhost:8761"

Write-Host ""
Write-Host "Startup script completed!" -ForegroundColor Green
Write-Host "Check the individual PowerShell windows for service logs." -ForegroundColor Yellow
