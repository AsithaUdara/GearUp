# PowerShell script to deploy GearUp Backend to Kubernetes (Docker Desktop)
# This script deploys all services to a local Kubernetes cluster

param(
    [switch]$SkipBuild,
    [switch]$DeleteFirst
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Backend - Kubernetes Deployer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to k8s-deployment directory
Set-Location $PSScriptRoot

# Check if kubectl is available
Write-Host "Checking Kubernetes availability..." -ForegroundColor Yellow
try {
    kubectl version --client --short 2>&1 | Out-Null
    Write-Host "✓ kubectl is available" -ForegroundColor Green
} catch {
    Write-Host "✗ kubectl is not available. Please install kubectl." -ForegroundColor Red
    exit 1
}

# Check if Kubernetes cluster is running
try {
    kubectl cluster-info 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Cluster not reachable"
    }
    Write-Host "✓ Kubernetes cluster is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Kubernetes cluster is not running. Please start Kubernetes in Docker Desktop." -ForegroundColor Red
    Write-Host "  Go to Docker Desktop Settings → Kubernetes → Enable Kubernetes" -ForegroundColor Yellow
    exit 1
}

# Delete existing deployment if requested
if ($DeleteFirst) {
    Write-Host ""
    Write-Host "Deleting existing deployment..." -ForegroundColor Yellow
    kubectl delete namespace gearup --ignore-not-found=true
    Write-Host "Waiting for namespace deletion..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

# Build Docker images if not skipped
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "Building Docker images..." -ForegroundColor Yellow
    Set-Location (Split-Path $PSScriptRoot -Parent)
    & "$PSScriptRoot\build-images.ps1"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Image build failed. Aborting deployment." -ForegroundColor Red
        exit 1
    }
    Set-Location $PSScriptRoot
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploying to Kubernetes..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Deploy in order

# 1. Namespace
Write-Host ""
Write-Host "[1/9] Creating namespace..." -ForegroundColor Yellow
kubectl apply -f 00-namespace.yaml
Start-Sleep -Seconds 2

# 2. Secrets
Write-Host "[2/9] Creating secrets..." -ForegroundColor Yellow
kubectl apply -f 01-secrets.yaml

# Check if Firebase secret needs to be created
Write-Host "  Checking Firebase secret..." -ForegroundColor Yellow
$firebaseSecretExists = kubectl get secret firebase-secret -n gearup 2>$null
if (-not $firebaseSecretExists) {
    Write-Host "  ⚠ Firebase secret is using placeholder. Update it with:" -ForegroundColor Yellow
    Write-Host "  kubectl create secret generic firebase-secret --from-file=firebase-service-account.json=path/to/your/firebase.json -n gearup --dry-run=client -o yaml | kubectl apply -f -" -ForegroundColor Cyan
}

# 3. ConfigMaps
Write-Host "[3/9] Creating configmaps..." -ForegroundColor Yellow
kubectl apply -f 02-configmaps.yaml

# 4. PostgreSQL
Write-Host "[4/9] Deploying PostgreSQL..." -ForegroundColor Yellow
kubectl apply -f 03-postgres.yaml
Write-Host "  Waiting for PostgreSQL to be ready (this may take 60-90 seconds)..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=postgres -n gearup --timeout=180s 2>$null

# 5. RabbitMQ
Write-Host "[5/9] Deploying RabbitMQ..." -ForegroundColor Yellow
kubectl apply -f 04-rabbitmq.yaml
Write-Host "  Waiting for RabbitMQ to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=rabbitmq -n gearup --timeout=120s 2>$null

# 6. Redis
Write-Host "[6/9] Deploying Redis..." -ForegroundColor Yellow
kubectl apply -f 05-redis.yaml
kubectl wait --for=condition=ready pod -l app=redis -n gearup --timeout=60s 2>$null

# 7. Ollama
Write-Host "[7/9] Deploying Ollama..." -ForegroundColor Yellow
kubectl apply -f 06-ollama.yaml

# 8. Infrastructure Services (Eureka, Config Server)
Write-Host "[8/9] Deploying infrastructure services..." -ForegroundColor Yellow
kubectl apply -f 07-service-discovery.yaml
Write-Host "  Waiting for Service Discovery to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
kubectl wait --for=condition=ready pod -l app=service-discovery -n gearup --timeout=180s 2>$null

kubectl apply -f 08-config-server.yaml
Write-Host "  Waiting for Config Server to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 20
kubectl wait --for=condition=ready pod -l app=config-server -n gearup --timeout=120s 2>$null

# 9. API Gateway and Microservices
Write-Host "[9/9] Deploying API Gateway and Microservices..." -ForegroundColor Yellow
kubectl apply -f 09-api-gateway.yaml

# If microservices manifest exists, deploy it
if (Test-Path "10-microservices.yaml") {
    kubectl apply -f 10-microservices.yaml
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deployment initiated!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Checking deployment status..." -ForegroundColor Yellow
Write-Host ""

# Show all pods
kubectl get pods -n gearup

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Access Information:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API Gateway: http://localhost:30080" -ForegroundColor Green
Write-Host "Eureka Dashboard: kubectl port-forward svc/service-discovery 8761:8761 -n gearup" -ForegroundColor Yellow
Write-Host "  Then visit: http://localhost:8761 (admin/password)" -ForegroundColor Yellow
Write-Host "RabbitMQ Management: kubectl port-forward svc/rabbitmq-service 15672:15672 -n gearup" -ForegroundColor Yellow
Write-Host "  Then visit: http://localhost:15672 (guest/guest)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Cyan
Write-Host "  View all pods: kubectl get pods -n gearup" -ForegroundColor White
Write-Host "  View services: kubectl get svc -n gearup" -ForegroundColor White
Write-Host "  View logs: kubectl logs -f <pod-name> -n gearup" -ForegroundColor White
Write-Host "  Delete deployment: kubectl delete namespace gearup" -ForegroundColor White
Write-Host ""
Write-Host "Note: Services may take 2-5 minutes to fully start and register with Eureka" -ForegroundColor Yellow
