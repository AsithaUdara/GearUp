# ========================================
# GearUp Backend - Service Health Check Script
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Services Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to check service health
function Test-ServiceHealth {
    param (
        [string]$ServiceName,
        [string]$Url
    )
    
    Write-Host "Checking $ServiceName..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host " $ServiceName is healthy (HTTP $($response.StatusCode))" -ForegroundColor Green
            return $true
        }
        else {
            Write-Host " $ServiceName returned HTTP $($response.StatusCode)" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host " $ServiceName is not responding" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$services = @(
    @{Name = "Automobile Service"; Url = "http://localhost:8080/actuator/health" },
    @{Name = "Notification Service"; Url = "http://localhost:8081/actuator/health" },
    @{Name = "User Auth Service"; Url = "http://localhost:8082/actuator/health" },
    @{Name = "Tracking Service"; Url = "http://localhost:8086/actuator/health" }
)

$healthyCount = 0
$unhealthyCount = 0

foreach ($service in $services) {
    if (Test-ServiceHealth -ServiceName $service.Name -Url $service.Url) {
        $healthyCount++
    }
    else {
        $unhealthyCount++
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Health Check Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Healthy: $healthyCount" -ForegroundColor Green
Write-Host "Unhealthy: $unhealthyCount" -ForegroundColor Red
Write-Host ""

if ($unhealthyCount -eq 0) {
    Write-Host "All services are healthy!" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "Some services are not healthy. Check the logs." -ForegroundColor Red
    exit 1
}
