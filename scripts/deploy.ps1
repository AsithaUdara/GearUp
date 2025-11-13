param(
    [switch]$Clean,
    [switch]$SkipBuild,
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Backend - Setup and Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

Write-Host "Project Root: $projectRoot" -ForegroundColor Gray
Write-Host ""

if ($Clean) {
    Write-Host "Step 1: Cleaning up old containers..." -ForegroundColor Yellow
    Set-Location "deployment/docker"
    docker-compose down -v 2>&1 | Out-Null
    Set-Location $projectRoot
    Write-Host "Cleanup completed" -ForegroundColor Green
    Write-Host ""
}

if (-not $SkipBuild) {
    Write-Host "Step 2: Building the project..." -ForegroundColor Yellow
    if ($SkipTests) {
        ./mvnw.cmd clean package -DskipTests
    }
    else {
        ./mvnw.cmd clean package
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Build completed successfully" -ForegroundColor Green
    Write-Host ""
}
else {
    Write-Host "Step 2: Skipping build" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Step 3: Starting Docker services..." -ForegroundColor Yellow
Set-Location "deployment/docker"

docker-compose build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed!" -ForegroundColor Red
    Set-Location $projectRoot
    exit 1
}

docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start containers!" -ForegroundColor Red
    Set-Location $projectRoot
    exit 1
}

Set-Location $projectRoot
Write-Host "Docker services started" -ForegroundColor Green
Write-Host ""

Write-Host "Step 4: Waiting for PostgreSQL..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$dbReady = $false

while ($attempt -lt $maxAttempts -and -not $dbReady) {
    $attempt++
    Write-Host "Attempt $attempt of $maxAttempts..." -ForegroundColor Gray
    
    docker exec gearup-postgres pg_isready -U postgres 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        $dbReady = $true
        Write-Host "PostgreSQL is ready" -ForegroundColor Green
    }
    else {
        Start-Sleep -Seconds 2
    }
}

if (-not $dbReady) {
    Write-Host "PostgreSQL failed to start" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "Step 5: Testing database connections..." -ForegroundColor Yellow
$testScript = Join-Path $projectRoot "scripts\test-db-connections.ps1"
& $testScript

if ($LASTEXITCODE -ne 0) {
    Write-Host "Database connection tests failed!" -ForegroundColor Red
    Set-Location "deployment/docker"
    docker-compose logs db
    Set-Location $projectRoot
    exit 1
}
Write-Host ""

Write-Host "Step 6: Waiting for services..." -ForegroundColor Yellow
Write-Host "This may take 30-60 seconds..." -ForegroundColor Gray
Start-Sleep -Seconds 45
Write-Host "Services should be ready" -ForegroundColor Green
Write-Host ""

Write-Host "Step 7: Performing health checks..." -ForegroundColor Yellow
$healthScript = Join-Path $projectRoot "scripts\health-check.ps1"
& $healthScript
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services are running at:" -ForegroundColor Green
Write-Host "  - Notification Service: http://localhost:8081" -ForegroundColor White
Write-Host "  - User Auth Service:    http://localhost:8082" -ForegroundColor White
Write-Host "  - PostgreSQL:           localhost:5432" -ForegroundColor White
Write-Host "  - RabbitMQ Management:  http://localhost:15672" -ForegroundColor White
Write-Host "  - Redis:                localhost:6379" -ForegroundColor White
Write-Host ""
