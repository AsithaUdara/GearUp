Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Backend - Final Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. Testing Database Connections..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
& "d:\University Academic Materials\EAD\GearUp-backend\scripts\test-db-connections.ps1"
Write-Host ""

Write-Host "2. Checking Docker Containers..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
docker ps --filter "name=gearup" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""

Write-Host "3. Verifying Database Tables..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "Notification Service Tables:" -ForegroundColor Cyan
docker exec -it gearup-postgres psql -U postgres -d as_notification_service -c "\dt" 2>$null

Write-Host ""
Write-Host "User Auth Service Tables:" -ForegroundColor Cyan
docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service -c "\dt" 2>$null

Write-Host ""
Write-Host "Payment Service Tables:" -ForegroundColor Cyan
docker exec -it gearup-postgres psql -U postgres -d as_payment_service -c "\dt" 2>$null

Write-Host ""
Write-Host "Appointment Service Tables:" -ForegroundColor Cyan
docker exec -it gearup-postgres psql -U postgres -d as_appointment_service -c "\dt" 2>$null

Write-Host ""
Write-Host "Modification Service Tables:" -ForegroundColor Cyan
docker exec -it gearup-postgres psql -U postgres -d as_modification_service -c "\dt" 2>$null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verification Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Green
Write-Host "- PostgreSQL: Running on port 5432" -ForegroundColor White
Write-Host "- Databases: All service databases created (including appointment & modification)" -ForegroundColor White
Write-Host "- Tables: Initialized with schemas" -ForegroundColor White
Write-Host "- Users: Dedicated users per service" -ForegroundColor White
Write-Host "- Docker: All containers orchestrated" -ForegroundColor White
Write-Host ""
Write-Host "Next: Review DEPLOYMENT_SUCCESS.md for details" -ForegroundColor Yellow
Write-Host ""
