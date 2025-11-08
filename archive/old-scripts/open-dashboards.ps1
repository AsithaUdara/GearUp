# Open All GearUp Dashboards and Tools
# Quick access to all running services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Opening GearUp Service Dashboards" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if services are running
$services = @{
    "User Auth Service" = "http://localhost:8082/actuator/health"
    "Eureka Dashboard" = "http://localhost:8761"
    "API Gateway" = "http://localhost:8080"
}

foreach ($service in $services.GetEnumerator()) {
    Write-Host "Checking $($service.Key)..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri $service.Value -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
        Write-Host "  ✅ Running - Opening..." -ForegroundColor Green
        Start-Sleep -Milliseconds 500
        Start-Process $service.Value
    } catch {
        Write-Host "  ❌ Not accessible" -ForegroundColor Red
    }
}

# Open documentation
Write-Host ""
Write-Host "Opening Documentation..." -ForegroundColor Yellow
$docsPath = "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\docs\DEPLOYMENT_COMPLETE.md"
if (Test-Path $docsPath) {
    Write-Host "  ✅ Opening DEPLOYMENT_COMPLETE.md" -ForegroundColor Green
    Start-Process "code" -ArgumentList $docsPath
}

# Open database tool (pgAdmin or command prompt)
Write-Host ""
Write-Host "Database Connection Info:" -ForegroundColor Yellow
Write-Host "  Host: localhost:5434" -ForegroundColor White
Write-Host "  Database: as_user_auth_service" -ForegroundColor White
Write-Host "  Username: postgres" -ForegroundColor White
Write-Host "  Password: Niro" -ForegroundColor White

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "All Dashboards Opened!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Quick Links:" -ForegroundColor Yellow
Write-Host "  Eureka: http://localhost:8761" -ForegroundColor White
Write-Host "  User Auth: http://localhost:8082/actuator/health" -ForegroundColor White
Write-Host "  API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host ""
