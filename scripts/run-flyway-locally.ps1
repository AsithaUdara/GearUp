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

# Accept a SecureString for password input to reduce accidental exposure; allow null and resolve from envs below
param(
    [string]$Service = "all",
    [System.Security.SecureString]$DbPassword = $null,
    [switch]$UseCompose,
    [switch]$KeepContainer
)

# Resolve DB password: prefer explicit parameter, then POSTGRES_PASSWORD env, then SPRING_DATASOURCE_PASSWORD env, else fallback
# No SecureString passed; look for environment variables (plain string) and convert into SecureString
if (-not $DbPassword) {
    if ($env:POSTGRES_PASSWORD) { $DbPassword = ConvertTo-SecureString $env:POSTGRES_PASSWORD -AsPlainText -Force }
    elseif ($env:SPRING_DATASOURCE_PASSWORD) { $DbPassword = ConvertTo-SecureString $env:SPRING_DATASOURCE_PASSWORD -AsPlainText -Force }
    else { $DbPassword = ConvertTo-SecureString 'postgres' -AsPlainText -Force }
}

# Convert SecureString to plain text for internal commands where needed (minimize exposure)
function Convert-SecureStringToPlain([System.Security.SecureString]$s) {
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
}

$PlainDbPassword = Convert-SecureStringToPlain $DbPassword

# Resolve Postgres bootstrap user (when starting container)
$PgBootstrapUser = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { 'postgres' }

# Helper to decide whether to use --env-file to avoid exposing password on command line
function Use-EnvFile() {
    return Test-Path -Path ".env"
}

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
    if (Use-EnvFile) {
        Write-Host "Using .env with docker compose"
        docker compose --env-file .env -f deployment/postgres/docker-compose.yml up -d
    }
    else {
        docker compose -f deployment/postgres/docker-compose.yml up -d
    }
}
else {
    Write-Host "Starting Postgres container (single)..."
    if (Use-EnvFile) {
        Write-Host "Using .env to provide Postgres credentials to container"
        docker run --name local_postgres --env-file .env -p 5432:5432 -d postgres:15 | Out-Null
    }
    else {
        # pass minimal required envs; avoid printing the password
        docker run --name local_postgres -e POSTGRES_USER=$PgBootstrapUser -e POSTGRES_PASSWORD=$PlainDbPassword -p 5432:5432 -d postgres:15 | Out-Null
    }
}

if ($UseCompose) {
    Write-Host "Waiting for Postgres inside docker-compose to be ready..."
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        try {
            & docker compose -f deployment/postgres/docker-compose.yml exec -T postgres bash -lc "pg_isready -h localhost -p 5432" | Out-Null; if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        }
        catch { }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { Write-Error "Postgres in compose did not become ready in time. Check Docker logs."; exit 1 }
}
else {
    if (-not (Wait-PostgresReady -User $PgBootstrapUser)) {
        Write-Error "Postgres did not become ready in time. Check Docker logs."
        exit 1
    }
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
    # sanitize service name for DB/user identifiers: replace non-alphanumeric/underscore with underscore
    $safeSvc = ($svc -replace '[^a-zA-Z0-9_]', '_')
    $dbName = "as_$safeSvc"
    $dbUser = "svc_$safeSvc"

    Write-Host "Creating DB and user for $svc (if not exists)"
    # Build idempotent SQL: create or alter role with password, create database if not exists
    $createSql = @"
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$dbUser') THEN
       CREATE ROLE $dbUser WITH LOGIN PASSWORD '$PlainDbPassword';
   ELSE
       ALTER ROLE $dbUser WITH PASSWORD '$PlainDbPassword';
   END IF;
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '$dbName') THEN
       CREATE DATABASE $dbName OWNER $dbUser;
   END IF;
END
$$;
"@
    if ($UseCompose) {
        # Run psql inside the container as the postgres OS user so no password is required (connects via socket)
        $argList = @('compose', '-f', 'deployment/postgres/docker-compose.yml', 'exec', '-T', '--user', 'postgres', 'postgres', 'psql', '-c', $createSql)
        $proc = Start-Process -FilePath 'docker' -ArgumentList $argList -NoNewWindow -Wait -PassThru
        if ($proc.ExitCode -ne 0) { Write-Host "User/database may already exist or creation failed (exit $($proc.ExitCode))" }
    }
    else {
        $argList = @('exec', '-i', '-u', 'postgres', 'local_postgres', 'psql', '-c', $createSql)
        $proc = Start-Process -FilePath 'docker' -ArgumentList $argList -NoNewWindow -Wait -PassThru
        if ($proc.ExitCode -ne 0) { Write-Host "User/database may already exist or creation failed (exit $($proc.ExitCode))" }
    }

    $flywayUrl = "jdbc:postgresql://localhost:5432/$dbName"
    Write-Host "Running Flyway migrate for $svc against $flywayUrl"

    # Pass the same Postgres password to Flyway
    & "./mvnw.cmd" "-Dflyway.url=$flywayUrl" "-Dflyway.user=$dbUser" "-Dflyway.password=$PlainDbPassword" -pl "services/$svc" flyway:migrate
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
