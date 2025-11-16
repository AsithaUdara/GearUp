<#
Wrapper script: create a Firebase user (using FIREBASE_API_KEY from .env), login to app,
run `api-gateway-event-tests.ps1` with the obtained app access token, then verify notifications.

Usage:
  Open PowerShell and run:
    powershell -ExecutionPolicy Bypass -File .\scripts\run_tests_with_auto_login.ps1

Requirements: curl.exe available, .env in repo root containing FIREBASE_API_KEY, API gateway running at http://localhost:9090
#>

Set-StrictMode -Version Latest

function Read-EnvFile {
    param([string]$Path)
    $result = @{}
    if (-not (Test-Path $Path)) { throw "Env file not found: $Path" }
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        if ($line -match '^(?:export\s+)?([A-Z0-9_]+)\s*=\s*(.*)$') {
            $k = $matches[1]
            $v = $matches[2].Trim()
            if ($v.StartsWith("'") -and $v.EndsWith("'")) { $v = $v.Substring(1, $v.Length-2) }
            elseif ($v.StartsWith('"') -and $v.EndsWith('"')) { $v = $v.Substring(1, $v.Length-2) }
            $result[$k] = $v
        }
    }
    return $result
}

function Invoke-CurlJson {
    param(
        [ValidateSet('GET','POST','PUT','DELETE','PATCH')][string]$Method,
        [string]$Url,
        [string]$JsonBody = $null,
        [string]$BearerToken = $null
    )
    $argList = @('-s','-X',$Method)
    $argList += ('-H', '"Content-Type: application/json"')
    if ($BearerToken) { $argList += ('-H', ('"Authorization: Bearer ' + $BearerToken + '"')) }
    if ($JsonBody -ne $null) { $argList += '--data-binary'; $argList += '@-' }
    $argList += $Url
    $argList += ('-w', "`n%{http_code}")

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = 'curl.exe'
    $psi.Arguments = [string]::Join(' ', $argList)
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $psi
    $started = $proc.Start()
    if (-not $started) { throw 'Failed to start curl.exe' }

    if ($JsonBody -ne $null) { $proc.StandardInput.Write($JsonBody); $proc.StandardInput.Close() }

    $raw = $proc.StandardOutput.ReadToEnd()
    $err = $proc.StandardError.ReadToEnd()
    $proc.WaitForExit()
    if ($proc.ExitCode -ne 0) { throw "curl.exe failed with exit code $($proc.ExitCode): $err" }

    $parts = $raw -split "`n"
    $status = $parts[-1]
    $body = ($parts[0..($parts.Length-2)] -join "`n").Trim()
    $firstObj = $body.IndexOf('{')
    $firstArr = $body.IndexOf('[')
    $first = -1
    if ($firstObj -ge 0 -and $firstArr -ge 0) { $first = [Math]::Min($firstObj,$firstArr) }
    elseif ($firstObj -ge 0) { $first = $firstObj } elseif ($firstArr -ge 0) { $first = $firstArr }
    if ($first -gt 0) { $body = $body.Substring($first) }
    $json = $null
    if ($body -ne '') {
        try { $json = $body | ConvertFrom-Json -ErrorAction Stop } catch { $json = $body }
    }
    return @{ status = [int]$status; body = $body; json = $json }
}

function Get-TokenFromObject {
    param($obj)
    if ($null -eq $obj) { return $null }
    if ($obj -isnot [System.Management.Automation.PSCustomObject]) { return $null }
    $names = $obj.PSObject.Properties.Name
    if ($names -contains 'accessToken') { return $obj.accessToken }
    if ($names -contains 'token') { return $obj.token }
    if ($names -contains 'data') { $t = Get-TokenFromObject $obj.data; if ($t) { return $t } }
    if ($names -contains 'tokens') { $t = Get-TokenFromObject $obj.tokens; if ($t) { return $t } }
    return $null
}

try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
    $repoRoot = Resolve-Path (Join-Path $scriptDir '..')
    $envFile = Join-Path $repoRoot '.env'
    Write-Host "Reading env file: $envFile" -ForegroundColor Cyan
    $env = Read-EnvFile -Path $envFile
    if (-not $env.ContainsKey('FIREBASE_API_KEY')) { throw 'FIREBASE_API_KEY not found in .env' }
    $firebaseApiKey = $env['FIREBASE_API_KEY']

    $apiBase = 'http://localhost:9090'

    # Create test Firebase user
    $suffix = [guid]::NewGuid().ToString().Split('-')[0]
    $userEmail = "auto.test+$suffix@example.com"
    $userPassword = 'TestPass123!'
    Write-Host "Creating Firebase user: $userEmail" -ForegroundColor Cyan
    $signupUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$firebaseApiKey"
    $signupPayload = @{ email = $userEmail; password = $userPassword; returnSecureToken = $true } | ConvertTo-Json -Compress
    $res = Invoke-CurlJson -Method POST -Url $signupUrl -JsonBody $signupPayload
    if ($res.status -ne 200) { Write-Host "Firebase signUp failed:" -ForegroundColor Red; Write-Host $res.body; throw 'Firebase signUp failed' }
    $signupBody = $res.json
    if ($signupBody -is [string]) { $signupBody = $signupBody | ConvertFrom-Json }
    $firebaseIdToken = $signupBody.idToken
    $firebaseUid = $signupBody.localId
    Write-Host "Created Firebase uid: $firebaseUid" -ForegroundColor Green

    # Register user in-app
    $regUrl = "$apiBase/api/v1/users/register"
    $regPayload = @{ firebaseUid = $firebaseUid; email = $userEmail; displayName = 'Auto Test' } | ConvertTo-Json -Compress
    $regRes = Invoke-CurlJson -Method POST -Url $regUrl -JsonBody $regPayload
    Write-Host "App register HTTP: $($regRes.status)" -ForegroundColor Cyan

    # Login to app using Firebase idToken
    Write-Host "Logging into app to obtain app access token" -ForegroundColor Cyan
    $loginUrl = "$apiBase/api/v1/auth/login"
    $loginPayload = @{ firebaseToken = $firebaseIdToken } | ConvertTo-Json -Compress
    $loginRes = Invoke-CurlJson -Method POST -Url $loginUrl -JsonBody $loginPayload
    if ($loginRes.status -ne 200) { Write-Host $loginRes.body -ForegroundColor Red; throw 'App login failed' }
    $parsed = $loginRes.json
    if ($parsed -is [string]) { $parsed = $parsed | ConvertFrom-Json }
    $accessToken = Get-TokenFromObject $parsed
    if (-not $accessToken) { Write-Host "Could not extract access token" -ForegroundColor Red; throw 'Missing access token' }
    $short = if ($accessToken.Length -gt 40) { $accessToken.Substring(0,40) + '...' } else { $accessToken }
    Write-Host "Obtained app access token (truncated): $short" -ForegroundColor Green

    $authHeader = "Bearer $accessToken"

    # Run the event tests (existing script). Many service endpoints validate Firebase ID tokens
    # (not the app-issued JWT), so pass the Firebase idToken as the Authorization header.
    Write-Host "Running API gateway event tests (using Firebase ID token for auth)..." -ForegroundColor Cyan
    & "$repoRoot\scripts\api-gateway-event-tests.ps1" -BaseUrl $apiBase -AuthHeader "Bearer $firebaseIdToken" -FirebaseUid $firebaseUid

    # Verification: query notification-service for unread notifications
    Write-Host "Verifying notifications for the test user..." -ForegroundColor Cyan
    $countRes = Invoke-CurlJson -Method GET -Url "$apiBase/api/v1/notifications/unread/count" -BearerToken $firebaseIdToken
    if ($countRes.status -eq 200) {
        $count = $countRes.json
        Write-Host "Unread notifications count:" -ForegroundColor Green -NoNewline; Write-Host " $($count.unreadCount)" -ForegroundColor Yellow
    } else { Write-Host "Failed to fetch unread count: $($countRes.status)" -ForegroundColor Red }

    $listRes = Invoke-CurlJson -Method GET -Url "$apiBase/api/v1/notifications/unread" -BearerToken $firebaseIdToken
    if ($listRes.status -eq 200) {
        $items = $listRes.json
        if ($items -is [System.Array] -and $items.Length -gt 0) {
            Write-Host "Unread notifications:" -ForegroundColor Green
            foreach ($n in $items) {
                $when = $n.createdAt -as [string]
                Write-Host "- [$($n.type)] $($n.title) -> $($n.body)" -ForegroundColor Yellow
            }
        } else { Write-Host "No unread notifications found." -ForegroundColor Yellow }
    } else { Write-Host "Failed to fetch unread notifications: $($listRes.status)" -ForegroundColor Red }

    # Test admin endpoints with admin user
    Write-Host "`nTesting admin endpoints with admin user..." -ForegroundColor Cyan
    $adminEmail = "admin@gearup.com"
    $adminPassword = "StrOng!Admin123"
    $adminFirebaseUid = "u2sgkfVpdTd9hkrUp5sb3ttiHOt2"

    # Login admin to Firebase
    Write-Host "Logging in admin user to Firebase..." -ForegroundColor Cyan
    $adminLoginUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$firebaseApiKey"
    $adminLoginPayload = @{ email = $adminEmail; password = $adminPassword; returnSecureToken = $true } | ConvertTo-Json -Compress
    $adminFirebaseRes = Invoke-CurlJson -Method POST -Url $adminLoginUrl -JsonBody $adminLoginPayload

    if ($adminFirebaseRes.status -ne 200) {
        Write-Host "Admin Firebase login failed: $($adminFirebaseRes.status)" -ForegroundColor Red
        Write-Host $adminFirebaseRes.body -ForegroundColor Red
    } else {
        $adminFirebaseBody = $adminFirebaseRes.json
        if ($adminFirebaseBody -is [string]) { $adminFirebaseBody = $adminFirebaseBody | ConvertFrom-Json }
        $adminFirebaseToken = $adminFirebaseBody.idToken
        Write-Host "Admin Firebase login successful" -ForegroundColor Green

        # Test admin payment request endpoint
        Write-Host "`nTesting admin payment request endpoint..." -ForegroundColor Yellow
        $paymentReqPayload = @{
            customerName = 'Jane Customer';
            customerEmail = 'jane@example.com';
            vehicleInfo = 'Toyota Corolla 2021';
            services = @(
                @{ code = 'SVC-1'; description = 'Oil change'; price = 49.99 }
            );
            submittedBy = 'admin-tests';
            submittedDate = (Get-Date).ToString('yyyy-MM-dd')
        } | ConvertTo-Json -Compress

        $paymentRes = Invoke-CurlJson -Method POST -Url "$apiBase/api/v1/payments/admin/requests" -JsonBody $paymentReqPayload -BearerToken $adminFirebaseToken
        if ($paymentRes.status -ge 200 -and $paymentRes.status -lt 300) {
            Write-Host "✓ Admin payment request created successfully (HTTP $($paymentRes.status))" -ForegroundColor Green
        } else {
            Write-Host "✗ Admin payment request failed (HTTP $($paymentRes.status))" -ForegroundColor Red
            if ($paymentRes.body) { Write-Host $paymentRes.body -ForegroundColor Gray }
        }

        # Test admin role assignment endpoint
        Write-Host "`nTesting admin role assignment endpoint..." -ForegroundColor Yellow
        $roleRes = Invoke-CurlJson -Method PUT -Url "$apiBase/api/v1/admin/users/assign-role?userId=$userEmail&role=TECHNICIAN" -BearerToken $adminFirebaseToken
        if ($roleRes.status -ge 200 -and $roleRes.status -lt 300) {
            Write-Host "✓ Admin role assignment successful (HTTP $($roleRes.status))" -ForegroundColor Green
        } else {
            Write-Host "✗ Admin role assignment failed (HTTP $($roleRes.status))" -ForegroundColor Red
            if ($roleRes.body) { Write-Host $roleRes.body -ForegroundColor Gray }
        }
    }

    Write-Host "`nAll tests completed." -ForegroundColor Green
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
