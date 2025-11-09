# Quick Start - Essential Services Only
# Starts only Config Server, Eureka, User Auth Service, and API Gateway

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Quick Start - Essential Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Start Config Server
Write-Host "1/4 Starting Config Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptDir\config-server'; mvn spring-boot:run"
Start-Sleep -Seconds 15

# Start Service Discovery
Write-Host "2/4 Starting Service Discovery..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptDir\service-discovery'; mvn spring-boot:run"
Start-Sleep -Seconds 15

# Start User Auth Service
Write-Host "3/4 Starting User Auth Service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptDir\services\user-auth-service'; mvn spring-boot:run"
Start-Sleep -Seconds 15

# Start API Gateway
Write-Host "4/4 Starting API Gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptDir\api-gateway'; mvn spring-boot:run"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "   Services Starting!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Wait 1-2 minutes for all services to fully start" -ForegroundColor Cyan
Write-Host ""
Write-Host "Check these URLs:" -ForegroundColor Yellow
Write-Host "  Config Server:       http://localhost:8888" -ForegroundColor White
Write-Host "  Service Discovery:   http://localhost:8761" -ForegroundColor White
Write-Host "  API Gateway:         http://localhost:8080" -ForegroundColor White
Write-Host ""
