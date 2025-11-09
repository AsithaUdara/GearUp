# GearUp Backend Services Startup Script
# Starts all microservices in the correct order

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   GearUp Backend Services Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get the script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# NOTE: Renamed helper function to avoid clashing with built-in PowerShell cmdlet Start-Service
# Starts a Maven Spring Boot module in a new PowerShell window.
function Start-GearService {
    param(
        [Parameter(Mandatory=$true)][string]$ServiceName,
        [Parameter(Mandatory=$true)][string]$ServicePath,
        [string]$Command = "mvn spring-boot:run"
    )

    Write-Host "Starting $ServiceName..." -ForegroundColor Yellow
    $fullPath = Join-Path $scriptDir $ServicePath

    if (Test-Path $fullPath) {
        # Use explicit Set-Location and expand variables directly (no nested single-quote confusion)
        $inner = "Set-Location `"$fullPath`"; Write-Host '[START] $ServiceName' -ForegroundColor Green; $Command"
        Start-Process powershell -ArgumentList '-NoExit','-Command', $inner | Out-Null
        Write-Host "✓ $ServiceName started in new window" -ForegroundColor Green
    } else {
        Write-Host "✗ Path not found: $fullPath" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}

# Start services in order
Write-Host "Step 1: Starting Config Server..." -ForegroundColor Cyan
Start-GearService -ServiceName "Config Server" -ServicePath "config-server"
Start-Sleep -Seconds 10

Write-Host ""; Write-Host "Step 2: Starting Service Discovery (Eureka)..." -ForegroundColor Cyan
Start-GearService -ServiceName "Service Discovery" -ServicePath "service-discovery"
Start-Sleep -Seconds 10

Write-Host ""; Write-Host "Step 3: Starting User Auth Service..." -ForegroundColor Cyan
Start-GearService -ServiceName "User Auth Service" -ServicePath "services\user-auth-service"
Start-Sleep -Seconds 10

Write-Host ""; Write-Host "Step 4: Starting API Gateway..." -ForegroundColor Cyan
Start-GearService -ServiceName "API Gateway" -ServicePath "api-gateway"
Start-Sleep -Seconds 5

# Optional: Start other services
Write-Host ""; Write-Host "Starting other services (if available)..." -ForegroundColor Cyan

# Newly added: Start Template Service (Service Templates CRUD) before optional others so admin CRUD works
if (Test-Path "services\template-service") {
    Start-GearService -ServiceName "Template Service" -ServicePath "services\template-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\vehicle-service") {
    Start-GearService -ServiceName "Vehicle Service" -ServicePath "services\vehicle-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\trip-service") {
    Start-GearService -ServiceName "Trip Service" -ServicePath "services\trip-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\billing-service") {
    Start-GearService -ServiceName "Billing Service" -ServicePath "services\billing-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\notification-service") {
    Start-GearService -ServiceName "Notification Service" -ServicePath "services\notification-service"
    Start-Sleep -Seconds 5
}

if (Test-Path "services\tracking-service") {
    Start-GearService -ServiceName "Tracking Service" -ServicePath "services\tracking-service"
    Start-Sleep -Seconds 5
}

Write-Host ""; Write-Host "========================================" -ForegroundColor Cyan
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
Write-Host 'Script completed. This launcher window can be closed.' -ForegroundColor Gray
