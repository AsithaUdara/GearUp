<#
dev-up.ps1
Builds the Java modules locally and brings up the docker-compose stack using the dev override
#>
param(
    [switch]$SkipBuild
)

Write-Host "Starting dev-up: will build jars locally and bring up Docker Compose (dev mode)"

if (-not $SkipBuild) {
    Write-Host "Running Maven to build service modules and local shared libs (this may take a few minutes)..."
    # Build the two services and any local shared modules (-am)
    & .\mvnw.cmd -T1C -DskipTests -DskipITs -pl services/automobile-service, services/notification-service -am package
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed. Fix compile errors first and re-run this script."
        exit $LASTEXITCODE
    }
}

Write-Host "Starting Docker Compose (dev override)"
docker compose -f deployment/docker/docker-compose.yml -f deployment/docker/docker-compose.dev.yml up --build -d

Write-Host "Done. Use 'docker compose -f deployment/docker/docker-compose.yml logs -f' to follow logs."
