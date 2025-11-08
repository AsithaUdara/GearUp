# Rebuild and Restart API Gateway with Chatbot Support

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Rebuilding API Gateway for Chatbot..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$apiGatewayPath = "d:\University Academic Materials\EAD\GearUp-backend\api-gateway"

# Navigate to API Gateway
Set-Location $apiGatewayPath

Write-Host "1. Cleaning previous build..." -ForegroundColor Yellow
mvn clean

Write-Host ""
Write-Host "2. Building API Gateway..." -ForegroundColor Yellow
mvn install -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Stop the current API Gateway if running" -ForegroundColor White
    Write-Host "2. Start API Gateway:" -ForegroundColor White
    Write-Host "   cd '$apiGatewayPath'" -ForegroundColor Gray
    Write-Host "   mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Verify Eureka registration:" -ForegroundColor White
    Write-Host "   http://localhost:8761" -ForegroundColor Gray
    Write-Host ""
    Write-Host "4. Test chatbot endpoint:" -ForegroundColor White
    Write-Host "   cd ..\scripts" -ForegroundColor Gray
    Write-Host "   .\test-chatbot-api.ps1" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "✗ Build failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above." -ForegroundColor Red
    Write-Host ""
}
