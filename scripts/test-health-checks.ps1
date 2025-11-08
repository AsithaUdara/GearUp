# Test Health Checks for Config Server and Eureka Server
# Run this script to verify the services are working locally

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Testing GearUp Service Health" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Change to docker directory
Set-Location -Path "deployment\docker"

# Stop any running containers
Write-Host "Stopping any existing containers..." -ForegroundColor Yellow
docker compose down
Write-Host ""

# Start infrastructure
Write-Host "Starting infrastructure services (PostgreSQL, Redis, RabbitMQ)..." -ForegroundColor Green
docker compose up -d db redis rabbitmq
Write-Host "Waiting 30 seconds for infrastructure..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check infrastructure
Write-Host "`nChecking infrastructure health:" -ForegroundColor Cyan
docker compose exec -T db pg_isready -U postgres
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ PostgreSQL is ready" -ForegroundColor Green
}
else {
    Write-Host "❌ PostgreSQL failed" -ForegroundColor Red
    exit 1
}

docker compose exec -T redis redis-cli ping
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Redis is ready" -ForegroundColor Green
}
else {
    Write-Host "❌ Redis failed" -ForegroundColor Red
    exit 1
}

# Start Config Server and Eureka
Write-Host "`nStarting Config Server and Eureka Server..." -ForegroundColor Green
docker compose up -d config-server service-discovery
Write-Host "Waiting 60 seconds for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Check Config Server Health
Write-Host "`nChecking Config Server health:" -ForegroundColor Cyan
$configHealthy = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8888/actuator/health" `
            -Credential (New-Object System.Management.Automation.PSCredential("configadmin", (ConvertTo-SecureString "change-me" -AsPlainText -Force))) `
            -UseBasicParsing -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Config Server is healthy!" -ForegroundColor Green
            Write-Host "Response: $($response.Content)" -ForegroundColor Gray
            $configHealthy = $true
            break
        }
    }
    catch {
        Write-Host "⏳ Waiting for Config Server... ($i/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $configHealthy) {
    Write-Host "❌ Config Server failed to become healthy" -ForegroundColor Red
    Write-Host "`nShowing Config Server logs:" -ForegroundColor Yellow
    docker compose logs config-server
    exit 1
}

# Check Eureka Server Health
Write-Host "`nChecking Eureka Server health:" -ForegroundColor Cyan
$eurekaHealthy = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" `
            -Credential (New-Object System.Management.Automation.PSCredential("admin", (ConvertTo-SecureString "password" -AsPlainText -Force))) `
            -UseBasicParsing -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Eureka Server is healthy!" -ForegroundColor Green
            Write-Host "Response: $($response.Content)" -ForegroundColor Gray
            $eurekaHealthy = $true
            break
        }
    }
    catch {
        Write-Host "⏳ Waiting for Eureka Server... ($i/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $eurekaHealthy) {
    Write-Host "❌ Eureka Server failed to become healthy" -ForegroundColor Red
    Write-Host "`nShowing Eureka Server logs:" -ForegroundColor Yellow
    docker compose logs service-discovery
    exit 1
}

# All checks passed
Write-Host "`n==================================" -ForegroundColor Green
Write-Host "✅ All health checks passed!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services are running at:" -ForegroundColor Cyan
Write-Host "  - Config Server: http://localhost:8888 (user: configadmin, pass: change-me)" -ForegroundColor White
Write-Host "  - Eureka Server: http://localhost:8761 (user: admin, pass: password)" -ForegroundColor White
Write-Host ""
Write-Host "To view logs:" -ForegroundColor Yellow
Write-Host "  docker compose logs config-server" -ForegroundColor Gray
Write-Host "  docker compose logs service-discovery" -ForegroundColor Gray
Write-Host ""
Write-Host "To stop services:" -ForegroundColor Yellow
Write-Host "  docker compose down" -ForegroundColor Gray
Write-Host ""
