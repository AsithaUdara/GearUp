<#
Run Flyway migrations locally against a Dockerized Postgres instance.

Usage examples:
  # Migrate all services (uses docker-compose)
  .\run-flyway-locally.ps1 -UseCompose -DbPassword 'changeme'

  # Migrate a single service
  .\run-flyway-locally.ps1 -Service notification-service -DbPassword 'secret'

This script will:
  - start Postgres (either docker-compose or single container)
  - wait until Postgres is ready
  - create DB/user per service if necessary
  - run Flyway migrate for each service
  - tear down containers unless -KeepContainer is specified
#>

param(
    [string]$Service = "all",
    [System.Security.SecureString]$DbPassword = (ConvertTo-SecureString 'postgresql' -AsPlainText -Force),
    [switch]$UseCompose,
    [switch]$KeepContainer
)

function Wait-PostgresReady {
    param($User)
    Write-Host "Waiting for Postgres to be ready (user=$User)..."
    for ($i = 0; $i -lt 30; $i++) {
        try {
            & pg_isready -h localhost -p 5432 -U $User | Out-Null; if ($LASTEXITCODE -eq 0) { return $true }
        }
        catch { }
        Start-Sleep -Seconds 2
    }
    return $false
}

if ($UseCompose) {
    Write-Host "Starting Postgres via docker-compose..."
    docker compose -f deployment/postgres/docker-compose.yml up -d
}
else {
    Write-Host "Starting Postgres container (single)..."
    docker run --name local_postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=$DbPassword -p 5432:5432 -d postgres:15 | Out-Null
}

if (-not (Wait-PostgresReady -User 'postgres')) {
    Write-Error "Postgres did not become ready in time. Check Docker logs."
    exit 1
}

# Determine services to migrate
if ($Service -eq "all") {
    $serviceDirs = Get-ChildItem -Directory services | Where-Object { Test-Path "$($_.FullName)\src\main\resources\db\migration" }
}
else {
    $serviceDirs = @()
    $sd = Get-Item -LiteralPath "services\$Service" -ErrorAction SilentlyContinue
    if ($sd -and (Test-Path "$sd\src\main\resources\db\migration")) { $serviceDirs += $sd }
}

foreach ($sd in $serviceDirs) {
    $svc = $sd.Name
    $dbName = "as_$svc"
    $dbUser = "svc_$svc"

    Write-Host "Creating DB and user for $svc (if not exists)"
    if ($UseCompose) {
        & docker compose -f deployment/postgres/docker-compose.yml exec -T postgres psql -U postgres -c "CREATE USER $dbUser WITH PASSWORD '$DbPassword';"
        if ($LASTEXITCODE -ne 0) { Write-Host "User may already exist or creation failed (exit $LASTEXITCODE)" }
        & docker compose -f deployment/postgres/docker-compose.yml exec -T postgres psql -U postgres -c "CREATE DATABASE $dbName OWNER $dbUser;"
        if ($LASTEXITCODE -ne 0) { Write-Host "Database may already exist or creation failed (exit $LASTEXITCODE)" }
    }
    else {
        & docker exec local_postgres psql -U postgres -c "CREATE USER $dbUser WITH PASSWORD '$DbPassword';"
        if ($LASTEXITCODE -ne 0) { Write-Host "User may already exist or creation failed (exit $LASTEXITCODE)" }
        & docker exec local_postgres psql -U postgres -c "CREATE DATABASE $dbName OWNER $dbUser;"
        if ($LASTEXITCODE -ne 0) { Write-Host "Database may already exist or creation failed (exit $LASTEXITCODE)" }
    }

    $flywayUrl = "jdbc:postgresql://localhost:5432/$dbName"
    Write-Host "Running Flyway migrate for $svc against $flywayUrl"
    & "./mvnw.cmd" "-Dflyway.url=$flywayUrl" "-Dflyway.user=$dbUser" "-Dflyway.password=$DbPassword" -pl "services/$svc" flyway:migrate
}

if (-not $KeepContainer) {
    if ($UseCompose) {
        docker compose -f deployment/postgres/docker-compose.yml down
    }
    else {
        docker stop local_postgres | Out-Null
        docker rm local_postgres | Out-Null
    }
}

Write-Host "Done."
