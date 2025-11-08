# GearUp Backend - Stop Script
# This script stops all services cleanly

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host " GearUp Backend - Stopping All Services" -ForegroundColor Cyan
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

# Navigate to docker directory
Set-Location "deployment\docker"

Write-Host "Stopping services..." -ForegroundColor Yellow
docker-compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host "v All services stopped" -ForegroundColor Green
}
else {
    Write-Host "ERROR: Failed to stop some services" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "To remove data volumes: " -NoNewline
Write-Host "docker-compose down -v" -ForegroundColor Yellow
Write-Host "To remove images too:   " -NoNewline
Write-Host "docker-compose down -v --rmi all" -ForegroundColor Yellow
