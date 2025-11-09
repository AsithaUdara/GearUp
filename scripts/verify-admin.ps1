# Verify Admin User and Endpoints
# Usage: run after starting docker compose and frontend login as admin@gearup.com
# Requires: You are logged in via Firebase (have email/password) or have an ID token copied.

param(
  [string]$Email = "admin@gearup.com",
  [string]$Password = "AdminInitialPass123!", # adjust if changed
  [string]$GatewayBase = "http://localhost:8088",
  [string]$FirebaseApiKey, # optionally supply Firebase Web API key to auto-fetch token
  [switch]$VerboseLogin
)

# Try to resolve FirebaseApiKey from environment if not passed
if (-not $FirebaseApiKey) {
  if ($env:NEXT_PUBLIC_FIREBASE_API_KEY) { $FirebaseApiKey = $env:NEXT_PUBLIC_FIREBASE_API_KEY }
  elseif ($env:FIREBASE_API_KEY) { $FirebaseApiKey = $env:FIREBASE_API_KEY }
}

Write-Host "[1/5] Obtain Firebase ID token" -ForegroundColor Cyan
if ($FirebaseApiKey) {
  Write-Host "    Attempting programmatic Firebase email/password sign-in" -ForegroundColor Yellow
  try {
    $loginBody = @{ email = $Email; password = $Password; returnSecureToken = $true } | ConvertTo-Json -Compress
    $loginResponse = Invoke-RestMethod -Method Post -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FirebaseApiKey" -ContentType 'application/json' -Body $loginBody
    if ($VerboseLogin) { Write-Host "    Firebase response keys: $($loginResponse.PSObject.Properties.Name -join ',')" -ForegroundColor DarkGray }
    $token = $loginResponse.idToken
    if ($token) {
      Write-Host "    Acquired ID token via API" -ForegroundColor Green
    } else {
      Write-Warning "    Login response did not contain idToken; falling back to manual paste"
    }
  } catch {
    Write-Warning "    Programmatic Firebase login failed: $($_.Exception.Message)"
  }
}

if (-not $token) {
  Write-Host "    Fallback: open frontend, login, then run getIdToken snippet in browser console." -ForegroundColor Yellow
  $token = Read-Host "Paste Firebase ID token for $Email"
}

if (-not $token) { Write-Error "Token is required"; exit 1 }

$headers = @{ Authorization = "Bearer $token" }

Write-Host "[2/5] Check profile" -ForegroundColor Cyan
try {
  $profile = Invoke-RestMethod -Uri "$GatewayBase/api/v1/users/profile" -Headers $headers -Method GET
  $roleNames = ($profile.data.roles | ForEach-Object { $_.name }) -join ','
  Write-Host "    User ID: $($profile.data.id) Roles: $roleNames" -ForegroundColor Green
} catch { Write-Warning "    Failed: $_" }

Write-Host "[3/5] List admin users" -ForegroundColor Cyan
try {
  $users = Invoke-RestMethod -Uri "$GatewayBase/api/v1/admin/users?page=0&size=5" -Headers $headers -Method GET
  $adminFound = $false
  foreach ($u in $users.data.content) {
    if ($u.email -eq $Email) { $adminFound = $true }
    Write-Host "    -> $($u.email) [$($u.role)] status=$($u.status)" -ForegroundColor White
  }
  if ($adminFound) { Write-Host "    Admin user $Email is present." -ForegroundColor Green } else { Write-Warning "    Admin user $Email not in first page." }
} catch { Write-Warning "    Failed: $_" }

Write-Host "[4/5] Fetch user stats" -ForegroundColor Cyan
try {
  $stats = Invoke-RestMethod -Uri "$GatewayBase/api/v1/admin/users/stats" -Headers $headers -Method GET
  Write-Host "    TotalUsers=$($stats.data.totalUsers) Admins=$($stats.data.totalAdmins)" -ForegroundColor Green
} catch { Write-Warning "    Failed: $_" }

Write-Host "[5/5] Done" -ForegroundColor Cyan
