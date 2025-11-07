# Quick Start Guide for Testing Admin APIs

## Prerequisites Check
Write-Host "`n=== CHECKING PREREQUISITES ===" -ForegroundColor Cyan

# Check if User Auth Service is running
Write-Host "`n1. Checking User Auth Service..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -Method GET -ErrorAction Stop
    Write-Host "   ✅ User Auth Service is running (Status: $($health.status))" -ForegroundColor Green
} catch {
    Write-Host "   ❌ User Auth Service is NOT running!" -ForegroundColor Red
    Write-Host "   Start it with: cd 'c:\Users\ASUS\Desktop\Final EAD\GearUp-backend' ; .\mvnw spring-boot:run -pl services/user-auth-service" -ForegroundColor Yellow
    exit
}

# Check if PostgreSQL is accessible
Write-Host "`n2. Checking PostgreSQL Database..." -ForegroundColor Yellow
try {
    # Use environment variable or fallback to default
    $pgPassword = if ($env:POSTGRES_PASSWORD) { $env:POSTGRES_PASSWORD } else { 'postgres' }
    $env:PGPASSWORD = $pgPassword
    $result = & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "SELECT COUNT(*) FROM users;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ PostgreSQL Database is accessible" -ForegroundColor Green
    } else {
        Write-Host "   ❌ PostgreSQL Database error!" -ForegroundColor Red
    }
} catch {
    Write-Host "   ⚠️  Could not verify PostgreSQL" -ForegroundColor Yellow
}

Write-Host "`n=== NEXT STEPS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Your backend is ready! Now you need to:" -ForegroundColor White
Write-Host ""
Write-Host "1️⃣  START YOUR FRONTEND" -ForegroundColor Yellow
Write-Host "    cd 'c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent'" -ForegroundColor Gray
Write-Host "    npm run dev" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  LOGIN AS ADMIN in your browser" -ForegroundColor Yellow
Write-Host "    Open: http://localhost:3000" -ForegroundColor Gray
Write-Host "    Login with admin credentials" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  GET FIREBASE TOKEN" -ForegroundColor Yellow
Write-Host "    Open browser console (F12)" -ForegroundColor Gray
Write-Host "    Run this command:" -ForegroundColor Gray
Write-Host "    firebase.auth().currentUser.getIdToken().then(t => console.log(t))" -ForegroundColor Cyan
Write-Host "    Copy the long token string" -ForegroundColor Gray
Write-Host ""
Write-Host "4️⃣  TEST THE ADMIN API" -ForegroundColor Yellow
Write-Host "    Open: .\scripts\test-admin-api.ps1" -ForegroundColor Gray
Write-Host "    Replace: YOUR_FIREBASE_TOKEN_HERE with your actual token" -ForegroundColor Gray
Write-Host "    Run: .\scripts\test-admin-api.ps1" -ForegroundColor Gray
Write-Host ""
Write-Host "5️⃣  INTEGRATE WITH FRONTEND" -ForegroundColor Yellow
Write-Host "    See: docs/ADMIN_USER_MANAGEMENT_API.md" -ForegroundColor Gray
Write-Host "    Copy the useAdminUsers hook to your frontend" -ForegroundColor Gray
Write-Host ""
Write-Host "=== DOCUMENTATION ===" -ForegroundColor Cyan
Write-Host "  📄 docs/ADMIN_USER_MANAGEMENT_API.md  - Complete API reference" -ForegroundColor White
Write-Host "  📄 scripts/test-admin-api.ps1         - Test script (needs token)" -ForegroundColor White
Write-Host ""
