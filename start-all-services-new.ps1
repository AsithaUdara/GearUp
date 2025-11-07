# GearUp Backend Services Startup Script
# Starts all microservices in the correct order

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   GearUp Backend Services Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get the script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Function to start a service in a new window
function Start-ServiceWindow {
    param(
        [string]$ServiceName,
        [string]$ServicePath
    )
    
    Write-Host "Starting $ServiceName..." -ForegroundColor Yellow
    $fullPath = Join-Path $scriptDir $ServicePath
    
    if (Test-Path $fullPath) {
        $command = "cd '$fullPath'; Write-Host 'Starting $ServiceName...' -ForegroundColor Green; mvn spring-boot:run"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $command
        Write-Host "✓ $ServiceName started in new window" -ForegroundColor Green
    } else {
        Write-Host "✗ Path not found: $fullPath" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 2
}

# Start services in order
Write-Host "Step 1: Starting Config Server..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Config Server" -ServicePath "config-server"
Start-Sleep -Seconds 10

Write-Host "`nStep 2: Starting Service Discovery (Eureka)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Service Discovery" -ServicePath "service-discovery"
Start-Sleep -Seconds 10

Write-Host "`nStep 3: Starting User Auth Service..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "User Auth Service" -ServicePath "services\user-auth-service"
Start-Sleep -Seconds 10

Write-Host "`nStep 4: Starting API Gateway..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "API Gateway" -ServicePath "api-gateway"
Start-Sleep -Seconds 5

# Optional: Start other services
Write-Host "`nStarting other services (if available)..." -ForegroundColor Cyan

if (Test-Path "services\vehicle-service") {
    Start-ServiceWindow -ServiceName "Vehicle Service" -ServicePath "services\vehicle-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\trip-service") {
    Start-ServiceWindow -ServiceName "Trip Service" -ServicePath "services\trip-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\billing-service") {
    Start-ServiceWindow -ServiceName "Billing Service" -ServicePath "services\billing-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\notification-service") {
    Start-ServiceWindow -ServiceName "Notification Service" -ServicePath "services\notification-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\tracking-service") {
    Start-ServiceWindow -ServiceName "Tracking Service" -ServicePath "services\tracking-service"
    Start-Sleep -Seconds 5
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   All Services Started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service URLs:" -ForegroundColor Yellow
Write-Host "  Config Server:       http://localhost:8888" -ForegroundColor White
Write-Host "  Service Discovery:   http://localhost:8761" -ForegroundColor White
Write-Host "  API Gateway:         http://localhost:8080" -ForegroundColor White
Write-Host "  User Auth Service:   http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "Tip: Check each window to see if services started successfully" -ForegroundColor Cyan
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
