# GearUp Backend - Startup Script
# This script starts all services with proper environment variable loading

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host " GearUp Backend - Starting All Services" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root (one level up from scripts folder)
$scriptDir = $PSScriptRoot
if ([string]::IsNullOrEmpty($scriptDir)) {
    $scriptDir = Get-Location
}

# Go to parent directory (project root)
$projectRoot = Split-Path $scriptDir -Parent
Set-Location $projectRoot

Write-Host "Project root: $projectRoot" -ForegroundColor Gray
Write-Host ""

# Check if deployment/docker/.env file exists
if (-not (Test-Path "deployment\docker\.env")) {
    Write-Host "ERROR: .env file not found in deployment/docker directory!" -ForegroundColor Red
    Write-Host "Current location: $(Get-Location)" -ForegroundColor Gray
    Write-Host "Please ensure .env file exists in: $projectRoot\deployment\docker" -ForegroundColor Yellow
    Write-Host "You can copy from .env.example: Copy-Item deployment\docker\.env.example deployment\docker\.env" -ForegroundColor Yellow
    exit 1
}

Write-Host "[1/3] Checking Docker..." -ForegroundColor Yellow
docker --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not installed or not running!" -ForegroundColor Red
    Write-Host "Please install Docker Desktop and make sure it's running." -ForegroundColor Yellow
    exit 1
}
Write-Host "v Docker is running" -ForegroundColor Green
Write-Host ""

Write-Host "[2/3] Starting services..." -ForegroundColor Yellow
Set-Location "deployment\docker"

# Stop any existing containers
docker-compose down 2>$null

# Start all services
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to start services!" -ForegroundColor Red
    exit 1
}

Write-Host "v Services started successfully" -ForegroundColor Green
Write-Host ""

Write-Host "[3/3] Waiting for services to be ready..." -ForegroundColor Yellow
Write-Host "This may take 30-60 seconds..." -ForegroundColor Gray

# Wait for database to be healthy
$maxAttempts = 30
$attempt = 0
while ($attempt -lt $maxAttempts) {
    $attempt++
    $dbStatus = docker inspect --format='{{.State.Health.Status}}' gearup-postgres 2>$null
    
    if ($dbStatus -eq "healthy") {
        Write-Host "v Database is healthy" -ForegroundColor Green
        break
    }
    
    Write-Host "  Waiting for database... ($attempt/$maxAttempts)" -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

if ($attempt -ge $maxAttempts) {
    Write-Host "WARNING: Database health check timed out" -ForegroundColor Yellow
    Write-Host "Check logs: docker-compose logs db" -ForegroundColor Gray
}

# Wait a bit more for services to start
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host " Services Status" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

docker-compose ps

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host " Access URLs" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service Discovery: " -NoNewline; Write-Host "http://localhost:8761" -ForegroundColor Green
Write-Host "API Gateway:       " -NoNewline; Write-Host "http://localhost:9090/actuator/health" -ForegroundColor Green
Write-Host "Config Server:     " -NoNewline; Write-Host "http://localhost:8888/actuator/health" -ForegroundColor Green
Write-Host "pgAdmin:           " -NoNewline; Write-Host "http://localhost:5050" -ForegroundColor Green
Write-Host "  └─ Email:        " -NoNewline; Write-Host "admin@gearup.com" -ForegroundColor Cyan
Write-Host "  └─ Password:     " -NoNewline; Write-Host "admin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "To view logs: " -NoNewline; Write-Host "docker-compose logs -f" -ForegroundColor Yellow
Write-Host "To stop all:  " -NoNewline; Write-Host "docker-compose down" -ForegroundColor Yellow
Write-Host ""
Write-Host "v Startup complete!" -ForegroundColor Green
