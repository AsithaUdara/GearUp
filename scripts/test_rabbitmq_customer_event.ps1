# Test script to validate RabbitMQ customer event publishing and notification-service consumption
# Usage: powershell -ExecutionPolicy Bypass -File .\scripts\test_rabbitmq_customer_event.ps1

Set-StrictMode -Version Latest

function Write-Info { param([string]$m) Write-Host $m -ForegroundColor Cyan }
function Write-Success { param([string]$m) Write-Host $m -ForegroundColor Green }
function Write-Warn { param([string]$m) Write-Host $m -ForegroundColor Yellow }
function Write-ErrorColor { param([string]$m) Write-Host $m -ForegroundColor Red }

try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
    Push-Location $scriptDir | Out-Null

    Write-Info "Recording start time"
    $startTime = Get-Date

    Write-Info "Running the standard Sarah journey script to create user+customer"
    # Run the main script and capture output
    $targetScript = Join-Path $scriptDir 'test_sarah_journey_curl.ps1'
    # Quote the script path in case it contains spaces
    $quotedTarget = '"' + $targetScript + '"'
    $psArgs = "-ExecutionPolicy", "Bypass", "-NoProfile", "-File", $quotedTarget
    $proc = Start-Process -FilePath powershell.exe -ArgumentList $psArgs -RedirectStandardOutput "output.txt" -RedirectStandardError "error.txt" -PassThru -Wait

    $out = Get-Content .\output.txt -Raw
    $err = Get-Content .\error.txt -Raw
    Write-Info "Primary script stdout summary:";
    $uidLine = $out -split "`n" | Where-Object { $_ -match 'Firebase uid:' } | Select-Object -First 1
    if ($uidLine) { Write-Host $uidLine } else { Write-Host "(no Firebase uid line in output)" }
    if ($err) { if ($err.Length -gt 0) { Write-Warn "Primary script stderr (truncated):"; Write-Warn ($err.Substring(0, [Math]::Min($err.Length, 400))) } }

    # Extract Firebase uid from output
    $m = [regex]::Match($out, 'Firebase uid: (\S+)')
    if (-not $m.Success) {
        Write-ErrorColor "Could not extract Firebase uid from script output. Aborting."
        Write-ErrorColor "Full output:"; Write-Host $out
        Exit 2
    }
    $firebaseUid = $m.Groups[1].Value
    Write-Success "Extracted Firebase uid: $firebaseUid"

    # Poll notification-service logs for the customer registered event
    $containerName = 'gearup-notification-service'
    Write-Info "Polling Docker logs for container: $containerName (since $($startTime.ToString('o')))"

    $found = $false
    $attempts = 0
    while ($attempts -lt 12 -and -not $found) {
        try {
            $logs = docker logs --since $($startTime.ToString('o')) $containerName --tail 500 2>&1 | Out-String
        }
        catch {
            Write-Warn "Failed to run 'docker logs'. Is Docker running and is container '$containerName' present?"
            Write-Warn "Error: $_"
            Exit 3
        }

        if ($logs -match $firebaseUid -or $logs -match 'Customer registered:') {
            Write-Success "Found event in notification logs (attempt $($attempts+1))."
            # show the matching lines
            $lines = $logs -split "`n" | Where-Object { $_ -match $firebaseUid -or $_ -match 'Customer registered:' }
            Write-Host "--- Matching log lines ---"; $lines | ForEach-Object { Write-Host $_ }
            $found = $true
            break
        }

        Start-Sleep -Seconds 2
        $attempts++
    }

    if (-not $found) {
        Write-ErrorColor "Did not find CustomerRegisteredEvent in $containerName logs after polling."
        Write-Host "Recent logs (last 500 lines since start):"; Write-Host $logs
        Exit 4
    }

    Write-Success "RabbitMQ event publish & consumption verified: CustomerRegisteredEvent observed by notification-service."
    Exit 0
}
catch {
    Write-ErrorColor "Script error: $_"
    Exit 1
}
finally {
    Pop-Location | Out-Null
}
