# PowerShell script to remove GearUp Backend from Kubernetes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GearUp Backend - Kubernetes Cleanup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "This will delete the entire 'gearup' namespace and all resources." -ForegroundColor Yellow
$confirm = Read-Host "Are you sure you want to continue? (yes/no)"

if ($confirm -ne "yes") {
    Write-Host "Cancelled." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Deleting namespace 'gearup'..." -ForegroundColor Yellow
kubectl delete namespace gearup

Write-Host ""
Write-Host "Cleanup complete!" -ForegroundColor Green
Write-Host "All GearUp resources have been removed from Kubernetes." -ForegroundColor Green
