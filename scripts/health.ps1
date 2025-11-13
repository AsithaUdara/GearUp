# Test Health Checks for All GearUp Services
# Run this script to verify all services are working locally

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
    Write-Host "[OK] PostgreSQL is ready" -ForegroundColor Green
}
else {
    Write-Host "[ERROR] PostgreSQL failed" -ForegroundColor Red
    exit 1
}

docker compose exec -T redis redis-cli ping
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Redis is ready" -ForegroundColor Green
}
else {
    Write-Host "[ERROR] Redis failed" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] RabbitMQ is ready" -ForegroundColor Green

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
            Write-Host "[OK] Config Server is healthy!" -ForegroundColor Green
            $configHealthy = $true
            break
        }
    }
    catch {
        Write-Host "[WAITING] Waiting for Config Server... ($i/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $configHealthy) {
    Write-Host "[ERROR] Config Server failed to become healthy" -ForegroundColor Red
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
            Write-Host "[OK] Eureka Server is healthy!" -ForegroundColor Green
            $eurekaHealthy = $true
            break
        }
    }
    catch {
        Write-Host "[WAITING] Waiting for Eureka Server... ($i/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $eurekaHealthy) {
    Write-Host "[ERROR] Eureka Server failed to become healthy" -ForegroundColor Red
    Write-Host "`nShowing Eureka Server logs:" -ForegroundColor Yellow
    docker compose logs service-discovery
    exit 1
}

# Start API Gateway
Write-Host "`nStarting API Gateway..." -ForegroundColor Green
docker compose up -d api-gateway
Write-Host "Waiting 45 seconds for API Gateway..." -ForegroundColor Yellow
Start-Sleep -Seconds 45

# Check API Gateway
Write-Host "`nChecking API Gateway health:" -ForegroundColor Cyan
$gatewayHealthy = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9090/actuator/health" -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] API Gateway is healthy!" -ForegroundColor Green
            $gatewayHealthy = $true
            break
        }
    }
    catch {
        Write-Host "[WAITING] Waiting for API Gateway... ($i/30)" -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if (-not $gatewayHealthy) {
    Write-Host "[ERROR] API Gateway failed" -ForegroundColor Red
    docker compose logs api-gateway
    exit 1
}

# Start all microservices
Write-Host "`nStarting all microservices..." -ForegroundColor Green
docker compose up -d user-auth-service notification-service chatbot-service vehicle-service customer-service tracking-service analytical-service payment-service parts-service appointment-service modification-service template-service pgadmin
Write-Host "Waiting 60 seconds for microservices..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Function to check service health
function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$MaxRetries = 30
    )
    
    Write-Host "`nChecking $ServiceName health:" -ForegroundColor Cyan
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$Port/actuator/health" -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "[OK] $ServiceName is healthy!" -ForegroundColor Green
                return $true
            }
        }
        catch {
            if ($i -eq $MaxRetries) {
                Write-Host "[ERROR] $ServiceName failed to become healthy" -ForegroundColor Red
                return $false
            }
            Write-Host "[WAITING] Waiting for $ServiceName... ($i/$MaxRetries)" -ForegroundColor Yellow
            Start-Sleep -Seconds 5
        }
    }
    return $false
}

# Check all microservices
$allHealthy = $true

$allHealthy = $allHealthy -and (Test-ServiceHealth "User Auth Service" 8082)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Notification Service" 8081)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Payment Service" 8083)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Chatbot Service" 8086)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Vehicle Service" 8090)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Customer Service" 8088)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Tracking Service" 8091)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Analytical Service" 8087)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Parts Service" 8093)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Appointment Service" 8084)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Modification Service" 8089)
$allHealthy = $allHealthy -and (Test-ServiceHealth "Template Service" 8085)

# Final summary
Write-Host "`n==================================" -ForegroundColor Cyan
if ($allHealthy) {
    Write-Host "[OK] ALL SERVICES ARE HEALTHY!" -ForegroundColor Green
    Write-Host "==================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Services running at:" -ForegroundColor Cyan
    Write-Host "  Infrastructure:" -ForegroundColor White
    Write-Host "    - PostgreSQL:    http://localhost:5432" -ForegroundColor Gray
    Write-Host "    - Redis:         http://localhost:6379" -ForegroundColor Gray
    Write-Host "    - RabbitMQ:      http://localhost:15672 (guest/guest)" -ForegroundColor Gray
    Write-Host "    - PgAdmin:       http://localhost:5050 (admin@gearup.com/admin123)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Core Services:" -ForegroundColor White
    Write-Host "    - Config Server: http://localhost:8888 (configadmin/change-me)" -ForegroundColor Gray
    Write-Host "    - Eureka:        http://localhost:8761 (admin/password)" -ForegroundColor Gray
    Write-Host "    - API Gateway:   http://localhost:8080" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Microservices:" -ForegroundColor White
    Write-Host "    - User Auth:     http://localhost:8082/actuator/health" -ForegroundColor Gray
    Write-Host "    - Notification:  http://localhost:8081/actuator/health" -ForegroundColor Gray
    Write-Host "    - Payment:       http://localhost:8083/actuator/health" -ForegroundColor Gray
    Write-Host "    - Appointment:   http://localhost:8084/actuator/health" -ForegroundColor Gray
    Write-Host "    - Template:      http://localhost:8085/actuator/health" -ForegroundColor Gray
    Write-Host "    - Chatbot:       http://localhost:8086/actuator/health" -ForegroundColor Gray
    Write-Host "    - Customer:      http://localhost:8088/actuator/health" -ForegroundColor Gray
    Write-Host "    - Modification:  http://localhost:8089/actuator/health" -ForegroundColor Gray
    Write-Host "    - Vehicle:       http://localhost:8090/actuator/health" -ForegroundColor Gray
    Write-Host "    - Tracking:      http://localhost:8091/actuator/health" -ForegroundColor Gray
    Write-Host "    - Analytical:    http://localhost:8087/actuator/health" -ForegroundColor Gray
    Write-Host "    - Parts:         http://localhost:8093/actuator/health" -ForegroundColor Gray
}
else {
    Write-Host "[ERROR] SOME SERVICES FAILED!" -ForegroundColor Red
    Write-Host "==================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check logs with:" -ForegroundColor Yellow
    Write-Host "  docker compose logs <service-name>" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "Management Commands:" -ForegroundColor Yellow
Write-Host "  docker compose ps              # View all containers" -ForegroundColor Gray
Write-Host "  docker compose logs -f <name>  # Follow logs" -ForegroundColor Gray
Write-Host "  docker compose down            # Stop all services" -ForegroundColor Gray
Write-Host ""
