<#
PowerShell script to test employee authentication and access to bookings endpoint.

This script demonstrates the employee auth flow:
  1. Create an EMPLOYEE account via dev admin endpoint (returns OTP)
  2. Use OTP to setup password via /api/v1/auth/setup-password (returns Firebase ID token)
  3. Call /api/v1/bookings with Firebase ID token to list all bookings

Usage: 
  powershell -ExecutionPolicy Bypass -File .\scripts\test_employee_bookings.ps1

Requirements:
  - API Gateway running on http://localhost:9090
  - user-auth-service and appointment-service running
  - .env file with FIREBASE_API_KEY and dev.admin.key configured
#>

Set-StrictMode -Version Latest

# --- Console color helpers
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
    param([string]$Path)
    $result = @{}
    if (-not (Test-Path $Path)) {
        throw "Env file not found: $Path"
    }
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        if ($line -match '^(?:export\s+)?([A-Z0-9_\.]+)\s*=\s*(.*)$') {
            $k = $matches[1]
            $v = $matches[2].Trim()
            # Strip surrounding quotes
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
        [string]$BearerToken = $null,
        [hashtable]$Headers = @{}
    )
    
    $argList = @('-s', '-X', $Method)
    $argList += ('-H', '"Content-Type: application/json"')
    
    if ($BearerToken) { 
        $argList += ('-H', ('"Authorization: Bearer ' + $BearerToken + '"')) 
    }
    
    foreach ($key in $Headers.Keys) {
        $argList += ('-H', ('"' + $key + ': ' + $Headers[$key] + '"'))
    }
    
    if ($JsonBody -ne $null) {
        $argList += '--data-binary'
        $argList += '@-'
    }
    
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
    
    # Trim anything before first JSON bracket
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

    if (-not $env.ContainsKey('dev.admin.key')) { 
        throw 'dev.admin.key not found in .env (required for dev employee creation)' 
    }
    $devAdminKey = $env['dev.admin.key']

    # API gateway base
    $apiBase = 'http://localhost:9090'

    # Generate unique employee email
    $suffix = [guid]::NewGuid().ToString().Split('-')[0]
    $employeeEmail = "mechanic+$suffix@gearup.com"
    $employeeName = "John Mechanic"
    $employeePassword = 'MechanicPass123!'

    # Step 1: Create EMPLOYEE account via dev admin endpoint
    Write-Info "`n=== Step 1: Create Employee Account ==="
    Write-Info "Creating employee: $employeeEmail with role EMPLOYEE"
    $createUrl = "$apiBase/api/v1/dev/admin/users/employees"
    $createPayload = @{ 
        email = $employeeEmail
        name = $employeeName
        role = 'EMPLOYEE'
        phoneNumber = '+1234567890'
    } | ConvertTo-Json -Compress
    
    $headers = @{ 'X-Dev-Admin-Key' = $devAdminKey }
    $res = Invoke-CurlJson -Method POST -Url $createUrl -JsonBody $createPayload -Headers $headers
    
    Write-Info "Create employee HTTP: $($res.status)"
    if ($res.status -ne 201) { 
        Write-ErrorColor "Failed to create employee"
        Write-ErrorColor $res.body
        throw 'Employee creation failed'
    }
    
    # Extract OTP from response
    $otpValue = $null
    $parsed = $res.json
    if ($parsed -is [string]) {
        try { $parsed = $parsed | ConvertFrom-Json -ErrorAction Stop } catch { }
    }
    
    # Response structure: { message, data: { otp, email, expiresAt } }
    if ($parsed -and $parsed.PSObject.Properties.Name -contains 'data') {
        $data = $parsed.data
        if ($data -and $data.PSObject.Properties.Name -contains 'otp') {
            $otpValue = $data.otp
        }
    }
    
    if (-not $otpValue) {
        Write-ErrorColor "Could not extract OTP from response:"
        Write-ErrorColor $res.body
        throw 'OTP extraction failed'
    }
    
    Write-Success "Employee created successfully!"
    Write-Success "OTP: $otpValue"

    # Step 2: Setup password using OTP (this returns Firebase ID token)
    Write-Info "`n=== Step 2: Setup Password and Login ==="
    Write-Info "Setting up password for: $employeeEmail"
    $setupUrl = "$apiBase/api/v1/auth/setup-password"
    $setupPayload = @{
        email = $employeeEmail
        otp = $otpValue
        password = $employeePassword
    } | ConvertTo-Json -Compress
    
    $res = Invoke-CurlJson -Method POST -Url $setupUrl -JsonBody $setupPayload
    
    Write-Info "Setup password HTTP: $($res.status)"
    if ($res.status -ne 200) {
        Write-ErrorColor "Failed to setup password"
        Write-ErrorColor $res.body
        throw 'Password setup failed'
    }
    
    # Extract Firebase ID token from response
    $firebaseIdToken = $null
    $parsed = $res.json
    if ($parsed -is [string]) {
        try { $parsed = $parsed | ConvertFrom-Json -ErrorAction Stop } catch { }
    }
    
    # Response structure: { message, data: { accessToken, refreshToken, ... } }
    # The accessToken IS the Firebase ID token
    if ($parsed -and $parsed.PSObject.Properties.Name -contains 'data') {
        $data = $parsed.data
        if ($data -and $data.PSObject.Properties.Name -contains 'accessToken') {
            $firebaseIdToken = $data.accessToken
        }
    }
    
    if (-not $firebaseIdToken) {
        Write-ErrorColor "Could not extract Firebase token from response:"
        Write-ErrorColor $res.body
        throw 'Token extraction failed'
    }
    
    $shortToken = if ($firebaseIdToken.Length -gt 40) { $firebaseIdToken.Substring(0,40) + '...' } else { $firebaseIdToken }
    Write-Success "Password setup successful!"
    Write-Success "Firebase ID token (truncated): $shortToken"

    # Step 3: Call bookings endpoint with Firebase ID token
    Write-Info "`n=== Step 3: Fetch All Bookings ==="
    Write-Info "GET $apiBase/api/v1/bookings (with Firebase ID token)"
    $bookingsUrl = "$apiBase/api/v1/bookings"
    $res = Invoke-CurlJson -Method GET -Url $bookingsUrl -BearerToken $firebaseIdToken
    
    Write-Info "Bookings HTTP: $($res.status)"
    if ($res.status -eq 200) {
        Write-Success "Successfully retrieved bookings!"
        Write-Host "`nBookings Response:"
        Write-Host "==================" -ForegroundColor Yellow
        
        # Pretty-print JSON
        try {
            $bookingsJson = $res.json
            if ($bookingsJson -is [string]) {
                $bookingsJson = $bookingsJson | ConvertFrom-Json
            }
            
            if ($bookingsJson -is [Array]) {
                Write-Host "Total bookings: $($bookingsJson.Count)" -ForegroundColor Green
                if ($bookingsJson.Count -gt 0) {
                    Write-Host "`nFirst few bookings:" -ForegroundColor Cyan
                    $bookingsJson | Select-Object -First 3 | ForEach-Object {
                        Write-Host "  - ID: $($_.id), Customer: $($_.customerName), Service: $($_.timeSlot.serviceName)" -ForegroundColor White
                    }
                    Write-Host "`nFull JSON response:" -ForegroundColor Cyan
                    $res.body | ConvertFrom-Json | ConvertTo-Json -Depth 10
                } else {
                    Write-Host "No bookings found in database" -ForegroundColor Yellow
                }
            } else {
                Write-Host $res.body
            }
        } catch {
            Write-Host $res.body
        }
        Write-Host "==================" -ForegroundColor Yellow
    } else {
        Write-Warn "Failed to retrieve bookings"
        Write-Warn "Response: $($res.body)"
    }

    Write-Success "`n=== Employee Authentication Journey Completed ==="
    Write-Host "`nSummary:" -ForegroundColor Cyan
    Write-Host "  Employee Email: $employeeEmail" -ForegroundColor White
    Write-Host "  Role: EMPLOYEE" -ForegroundColor White
    Write-Host "  Authentication: SUCCESS" -ForegroundColor Green
    Write-Host "  Bookings Access: $(if ($res.status -eq 200) { 'SUCCESS' } else { 'FAILED' })" -ForegroundColor $(if ($res.status -eq 200) { 'Green' } else { 'Red' })
}
catch {
    Write-ErrorColor "`nError: $_"
    Write-ErrorColor $_.ScriptStackTrace
    exit 1
}
