<#
PowerShell script that uses curl.exe to run Sarah's user journey.

Usage: Open PowerShell (Windows) and run:
  powershell -ExecutionPolicy Bypass -File .\scripts\test_sarah_journey_curl.ps1

The script reads the repository root `.env` for `FIREBASE_API_KEY` and uses
`http://localhost:9090` as the API gateway base URL. It performs these steps:
  1. Create a Firebase user (REST API) to obtain a Firebase ID token and uid
  2. Call `/api/v1/users/register` to register the user in the app
  3. Call `/api/v1/auth/login` with the Firebase ID token to get app access token
  4. Create a customer, call `users/me`, get customer, update user and customer

All HTTP calls are made with `curl.exe`. Outputs include HTTP status codes
and response bodies. No hardcoded app tokens are used.
#>

Set-StrictMode -Version Latest

# --- Console color helpers for readability
function Write-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Yellow
}

function Write-ErrorColor {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}


function Read-EnvFile {
    param(
        [string]$Path
    )
    $result = @{}
    if (-not (Test-Path $Path)) {
        throw "Env file not found: $Path"
    }
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        if ($line -match '^(?:export\s+)?([A-Z0-9_]+)\s*=\s*(.*)$') {
            $k = $matches[1]
            $v = $matches[2].Trim()
            # strip surrounding quotes if present
            if ($v.StartsWith("'") -and $v.EndsWith("'")) { $v = $v.Substring(1, $v.Length-2) }
            elseif ($v.StartsWith('"') -and $v.EndsWith('"')) { $v = $v.Substring(1, $v.Length-2) }
            $result[$k] = $v
        }
    }
    return $result
}

function Invoke-CurlJson {
    param(
        [ValidateSet('GET','POST','PUT','DELETE','PATCH')]
        [string]$Method,
        [string]$Url,
        [string]$JsonBody = $null,
        [string]$BearerToken = $null
    )
    # Build curl arguments. When sending JSON, provide it on stdin ("@-") to avoid
    # PowerShell/quoting issues on Windows which can strip quotes.
    # Build header arguments as single strings with proper quoting so curl receives them
    $argList = @('-s','-X',$Method)
    $argList += ('-H', '"Content-Type: application/json"')
    if ($BearerToken) { $argList += ('-H', ('"Authorization: Bearer ' + $BearerToken + '"')) }
    if ($JsonBody -ne $null) {
        $argList += '--data-binary'
        $argList += '@-'
    }
    $argList += $Url
    $argList += ('-w', "`n%{http_code}")

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = 'curl.exe'
    # Join arguments into a single string for ProcessStartInfo; ensure values containing spaces are quoted
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

    if ($JsonBody -ne $null) {
        $stdin = $proc.StandardInput
        $stdin.Write($JsonBody)
        $stdin.Close()
    }

    $raw = $proc.StandardOutput.ReadToEnd()
    $err = $proc.StandardError.ReadToEnd()
    $proc.WaitForExit()
    if ($proc.ExitCode -ne 0) {
        throw "curl.exe failed with exit code $($proc.ExitCode): $err"
    }

    $parts = $raw -split "`n"
    $status = $parts[-1]
    $body = ($parts[0..($parts.Length-2)] -join "`n").Trim()
    # Sometimes curl/stdout can include stray characters before JSON (e.g. chunk markers).
    # Trim anything before the first JSON object/array bracket so ConvertFrom-Json works.
    $firstObj = $body.IndexOf('{')
    $firstArr = $body.IndexOf('[')
    $first = -1
    if ($firstObj -ge 0 -and $firstArr -ge 0) { $first = [Math]::Min($firstObj,$firstArr) }
    elseif ($firstObj -ge 0) { $first = $firstObj }
    elseif ($firstArr -ge 0) { $first = $firstArr }
    if ($first -gt 0) { $body = $body.Substring($first) }
    $json = $null
    if ($body -ne '') {
        try { $json = $body | ConvertFrom-Json -ErrorAction Stop } catch { $json = $body }
    }
    return @{ status = [int]$status; body = $body; json = $json }
}

try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
    $repoRoot = Resolve-Path (Join-Path $scriptDir '..')
    $envFile = Join-Path $repoRoot '.env'
    Write-Info "Reading env file: $envFile"
    $env = Read-EnvFile -Path $envFile

    if (-not $env.ContainsKey('FIREBASE_API_KEY')) { throw 'FIREBASE_API_KEY not found in .env' }
    $firebaseApiKey = $env['FIREBASE_API_KEY']

    # API gateway base (production-like for the exercise)
    $apiBase = 'http://localhost:9090'

    # Make unique user email to avoid collisions
    $suffix = [guid]::NewGuid().ToString().Split('-')[0]
    $userEmail = "sarah.johnson+$suffix@example.com"
    $userName = 'Sarah Johnson'
    $userPhone = '+1234567890'
    $userPassword = 'SecurePass123!'

    Write-Info "Creating Firebase user for: $userEmail"
    $signupUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$firebaseApiKey"
    $signupPayload = @{ email = $userEmail; password = $userPassword; returnSecureToken = $true } | ConvertTo-Json -Compress
    $res = Invoke-CurlJson -Method POST -Url $signupUrl -JsonBody $signupPayload
    Write-Info "Firebase signUp HTTP: $($res.status)"
    Write-Info "Firebase signUp response body: $($res.body)"
    if ($res.status -ne 200) { Write-ErrorColor $res.body; throw 'Firebase signUp failed' }
    $signupBody = $res.json
    if ($signupBody -is [string]) {
        try { $signupBody = $signupBody | ConvertFrom-Json -ErrorAction Stop } catch { }
    }
    if ($null -eq $signupBody -or -not $signupBody.PSObject.Properties.Name -contains 'idToken') {
        Write-Host "Unexpected signup response body:"; Write-Host $res.body
        throw 'Firebase signUp response missing idToken'
    }
    $firebaseIdToken = $signupBody.idToken
    $firebaseUid = $signupBody.localId
    Write-Success "Firebase uid: $firebaseUid"

    # Register user in the app
    Write-Info "Registering user in app: $apiBase/api/v1/users/register"
    $regUrl = "$apiBase/api/v1/users/register"
    # The service expects RegisterUserRequest with firebaseUid, email and displayName
    $regPayload = @{ firebaseUid = $firebaseUid; email = $userEmail; displayName = $userName; phoneNumber = $userPhone } | ConvertTo-Json -Compress
    $res = Invoke-CurlJson -Method POST -Url $regUrl -JsonBody $regPayload
    Write-Info "App register HTTP: $($res.status)"
    if ($res.status -ge 400) { Write-Warn $res.body }

    # Login to app using Firebase ID token
    Write-Info "Logging into app using Firebase ID token: $apiBase/api/v1/auth/login"
    $loginUrl = "$apiBase/api/v1/auth/login"
    # AuthRequest expects field 'firebaseToken'
    $loginPayload = @{ firebaseToken = $firebaseIdToken } | ConvertTo-Json -Compress
    $res = Invoke-CurlJson -Method POST -Url $loginUrl -JsonBody $loginPayload
    Write-Info "App login HTTP: $($res.status)"
    if ($res.status -ne 200) { Write-ErrorColor $res.body; throw 'App login failed' }
    # Robustly extract access token from several possible response shapes, including
    # the ApiResponse wrapper used by the app (e.g. { message, data: { accessToken } }).
    $accessToken = $null
    $parsed = $res.json
    if ($parsed -is [string]) {
        try { $parsed = $parsed | ConvertFrom-Json -ErrorAction Stop } catch { 
            try { $parsed = $res.body | ConvertFrom-Json -ErrorAction Stop } catch { }
        }
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

    $accessToken = Get-TokenFromObject $parsed
    if (-not $accessToken) { Write-ErrorColor "Login response:"; Write-ErrorColor $res.body; throw 'Could not extract access token from login response' }
    if ($accessToken.Length -gt 40) { $short = $accessToken.Substring(0,40) + '...' } else { $short = $accessToken }
    Write-Success "Obtained app access token (truncated): $short"

    # 3: Create customer profile
    # Note: most services validate Firebase ID tokens (not the app-issued JWT). Use the Firebase idToken here.
    $custUrl = "$apiBase/api/v1/customers"
    $custPayload = @{ firebaseUid = $firebaseUid; name = $userName; email = $userEmail; phone = $userPhone } | ConvertTo-Json -Compress
    Write-Info "Creating customer: POST $custUrl (using Firebase ID token)"
    $res = Invoke-CurlJson -Method POST -Url $custUrl -JsonBody $custPayload -BearerToken $firebaseIdToken
    if ($res.status -ge 200 -and $res.status -lt 300) { Write-Success "Create customer HTTP: $($res.status)" } else { Write-Warn "Create customer HTTP: $($res.status)"; if ($res.body) { Write-Warn $res.body } }

    # 4: Get current user profile
    $meUrl = "$apiBase/api/v1/users/me"
    Write-Info "Getting current user: GET $meUrl (using Firebase ID token)"
    $res = Invoke-CurlJson -Method GET -Url $meUrl -BearerToken $firebaseIdToken
    if ($res.status -eq 200) { Write-Success "GET users/me HTTP: $($res.status)"; Write-Host ($res.body | Out-String) } else { Write-Warn "GET users/me HTTP: $($res.status)"; if ($res.body) { Write-Warn $res.body } }

    # 5: Get customer by firebaseUid
    $getCustUrl = "$apiBase/api/v1/customers/$firebaseUid"
    Write-Info "Getting customer: GET $getCustUrl (using Firebase ID token)"
    $res = Invoke-CurlJson -Method GET -Url $getCustUrl -BearerToken $firebaseIdToken
    if ($res.status -eq 200) { Write-Success "GET customers/{uid} HTTP: $($res.status)"; Write-Host ($res.body | Out-String) } else { Write-Warn "GET customers/{uid} HTTP: $($res.status)"; if ($res.body) { Write-Warn $res.body } }

    # 6: Update user profile with address
    $updateMePayload = @{ address = '123 Main St'; city = 'Springfield'; state = 'IL'; zipCode = '62701' } | ConvertTo-Json -Compress
    Write-Info "Updating user profile: PUT $meUrl (using Firebase ID token)"
    $res = Invoke-CurlJson -Method PUT -Url $meUrl -JsonBody $updateMePayload -BearerToken $firebaseIdToken
    if ($res.status -ge 200 -and $res.status -lt 300) { Write-Success "PUT users/me HTTP: $($res.status)" } else { Write-Warn "PUT users/me HTTP: $($res.status)"; if ($res.body) { Write-Warn $res.body } }

    # 7: Update customer profile
    $updateCustPayload = @{ address = '123 Main St'; city = 'Springfield'; state = 'IL' } | ConvertTo-Json -Compress
    Write-Info "Updating customer: PUT $getCustUrl (using Firebase ID token)"
    $res = Invoke-CurlJson -Method PUT -Url $getCustUrl -JsonBody $updateCustPayload -BearerToken $firebaseIdToken
    if ($res.status -ge 200 -and $res.status -lt 300) { Write-Success "PUT customers/{uid} HTTP: $($res.status)" } else { Write-Warn "PUT customers/{uid} HTTP: $($res.status)"; if ($res.body) { Write-Warn $res.body } }

    Write-Success "Sarah's journey completed. Check above HTTP codes and responses for success."
}
catch {
    Write-Error "Error: $_"
    exit 1
}
