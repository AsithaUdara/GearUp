# PowerShell script to build all Docker images for Kubernetes deployment
# This script builds Docker images for all GearUp microservices

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Backend - Docker Image Builder" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root
Set-Location (Split-Path $PSScriptRoot -Parent)

# Check if Docker is running
Write-Host "Checking Docker availability..." -ForegroundColor Yellow
docker info > $null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}
else {
    Write-Host "OK: Docker is running" -ForegroundColor Green
}

# Build all services
$services = @(
    @{Name = "service-discovery"; Path = "./service-discovery/Dockerfile"; Tag = "gearup/service-discovery:latest" }
    @{Name = "config-server"; Path = "./config-server/Dockerfile"; Tag = "gearup/config-server:latest" }
    @{Name = "api-gateway"; Path = "./api-gateway/Dockerfile"; Tag = "gearup/api-gateway:latest" }
    @{Name = "notification-service"; Path = "./services/notification-service/Dockerfile"; Tag = "gearup/notification-service:latest" }
    @{Name = "user-auth-service"; Path = "./services/user-auth-service/Dockerfile"; Tag = "gearup/user-auth-service:latest" }
    @{Name = "payment-service"; Path = "./services/payment-service/Dockerfile"; Tag = "gearup/payment-service:latest" }
    @{Name = "appointment-service"; Path = "./services/appointment-service/Dockerfile"; Tag = "gearup/appointment-service:latest" }
    @{Name = "template-service"; Path = "./services/template-service/Dockerfile"; Tag = "gearup/template-service:latest" }
    @{Name = "chatbot-service"; Path = "./services/chatbot-service/Dockerfile"; Tag = "gearup/chatbot-service:latest" }
    @{Name = "analytical-service"; Path = "./services/analytical-service/Dockerfile"; Tag = "gearup/analytical-service:latest" }
    @{Name = "customer-service"; Path = "./services/customer-service/Dockerfile"; Tag = "gearup/customer-service:latest" }
    @{Name = "tracking-service"; Path = "./services/tracking-service/Dockerfile"; Tag = "gearup/tracking-service:latest" }
    @{Name = "vehicle-service"; Path = "./services/vehicle-service/Dockerfile"; Tag = "gearup/vehicle-service:latest" }
    @{Name = "modification-service"; Path = "./services/modification-service/Dockerfile"; Tag = "gearup/modification-service:latest" }
    @{Name = "parts-service"; Path = "./services/parts-service/Dockerfile"; Tag = "gearup/parts-service:latest" }
)

Write-Host ""
Write-Host "Building Docker images..." -ForegroundColor Yellow
Write-Host "This may take 10-15 minutes depending on your system." -ForegroundColor Yellow
Write-Host ""

$successCount = 0
$failCount = 0

foreach ($service in $services) {
    Write-Host "Building $($service.Name)..." -ForegroundColor Cyan

    # Check if Dockerfile exists
    if (-not (Test-Path $service.Path)) {
        Write-Host "  ERROR: Dockerfile not found at $($service.Path)" -ForegroundColor Red
        $failCount++
        continue
    }

    # Build the image
    $buildOutput = docker build -f $service.Path -t $service.Tag . 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  OK: Built $($service.Tag)" -ForegroundColor Green
        $successCount++
    }
    else {
        Write-Host "  ERROR: Failed to build $($service.Name)" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Build Summary:" -ForegroundColor Cyan
Write-Host "  Successful: $successCount" -ForegroundColor Green
Write-Host "  Failed: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($failCount -gt 0) {
    Write-Host "Some images failed to build. Please check the errors above." -ForegroundColor Red
    exit 1
}
else {
    Write-Host "All images built successfully!" -ForegroundColor Green
    Write-Host "You can now deploy to Kubernetes using: .\k8s-deployment\deploy.ps1" -ForegroundColor Yellow
}
