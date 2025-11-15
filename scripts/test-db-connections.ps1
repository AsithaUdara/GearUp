Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp PostgreSQL Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$testsPassed = 0
$testsFailed = 0

Write-Host "Testing Notification Service..." -ForegroundColor Yellow
docker exec -it gearup-postgres psql -U postgres -d as_notification_service -c "SELECT current_database(), current_user;" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Notification Service connection: SUCCESS" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "Notification Service connection: FAILED" -ForegroundColor Red
    $testsFailed++
}
Write-Host ""

Write-Host "Testing User Auth Service..." -ForegroundColor Yellow
docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service -c "SELECT current_database(), current_user;" 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "User Auth Service connection: SUCCESS" -ForegroundColor Green
    $testsPassed++
}
else {
    Write-Host "User Auth Service connection: FAILED" -ForegroundColor Red
    $testsFailed++
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Passed: $testsPassed" -ForegroundColor Green
Write-Host "Failed: $testsFailed" -ForegroundColor Red
Write-Host ""

if ($testsFailed -eq 0) {
    Write-Host "All database connections are working!" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "Some database connections failed." -ForegroundColor Red
    exit 1
}
