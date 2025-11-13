<#
.SYNOPSIS
  CI helper: Verify microservice dirs are declared in docker-compose.

.DESCRIPTION
  Reads `deployment/docker/docker-compose.yml`, extracts the top-level
  `services:` keys, then compares with directories under `services/` that
  end with `-service`.

  Exits with code 1 when expected service directories are not present in
  the compose file (so CI will fail). Prints informational 'extras' when
  compose declares service names that don't have matching directories.

.USAGE
  pwsh -NoProfile -File .\scripts\ci\check_compose_services.ps1
#>

Set-StrictMode -Version Latest

function Write-ErrorAndExit([string]$msg, [int]$code = 1) {
    Write-Host $msg -ForegroundColor Red
    exit $code
}

$repoRoot = Split-Path -Path $PSScriptRoot -Parent  # scripts -> repo root

$composePath = Join-Path $repoRoot 'deployment\docker\docker-compose.yml'
$servicesDir = Join-Path $repoRoot 'services'

if (-not (Test-Path $composePath)) {
    Write-ErrorAndExit "Compose file not found: $composePath" 2
}

if (-not (Test-Path $servicesDir)) {
    Write-ErrorAndExit "Services directory not found: $servicesDir" 2
}

# Parse top-level services keys from docker-compose.yml using simple indentation-aware scan
$composeText = Get-Content -Raw -Encoding UTF8 $composePath
$lines = $composeText -split "`n"

$inServices = $false
$composeServices = @{}

foreach ($raw in $lines) {
    $line = $raw -replace "`r", ""
    if (-not $inServices) {
        if ($line -match '^[ \t]*services:\s*$') {
            $inServices = $true
        }
        continue
    }

    # if we hit another top-level key (non-indented, non-empty), stop scanning
    if ($line -match '^[^ \t]') {
        break
    }

    # match service entry lines that start with two spaces then the name and colon
    if ($line -match '^[ \t]{2}([a-zA-Z0-9._-]+):') {
        $name = $matches[1]
        $composeServices[$name] = $true
    }
}

$composeServiceNames = $composeServices.Keys

# Discover service directories under /services ending with -service
$svcDirs = Get-ChildItem -Path $servicesDir -Directory -ErrorAction Stop | ForEach-Object { $_.Name }
$expected = $svcDirs | Where-Object { $_ -like '*-service' }

$expectedSet = [System.Collections.Generic.HashSet[string]]::new()
foreach ($s in $expected) { [void]$expectedSet.Add($s) }

$composeSet = [System.Collections.Generic.HashSet[string]]::new()
foreach ($s in $composeServiceNames) { [void]$composeSet.Add($s) }

# Compute missing expected services
$missing = @()
foreach ($s in $expectedSet) { if (-not $composeSet.Contains($s)) { $missing += $s } }

# Compute extras declared in compose that don't have matching dirs (only consider names ending with -service)
$extras = @()
foreach ($s in $composeSet) { if ($s -like '*-service' -and -not $expectedSet.Contains($s)) { $extras += $s } }

if ($missing.Count -gt 0) {
    Write-Host "ERROR: The following service directories are NOT declared in docker-compose.yml:" -ForegroundColor Red
    foreach ($m in $missing | Sort-Object) { Write-Host "  - $m" }
    Write-Host "`nFailing CI due to missing services." -ForegroundColor Red
    exit 1
}

Write-Host "All expected microservice directories are declared in docker-compose.yml." -ForegroundColor Green

if ($extras.Count -gt 0) {
    Write-Host "`nNote: docker-compose.yml declares service names that don't have matching directories:" -ForegroundColor Yellow
    foreach ($e in $extras | Sort-Object) { Write-Host "  - $e" }
    Write-Host "If these are intentional (infrastructure containers), you can ignore this message." -ForegroundColor Yellow
}

exit 0
