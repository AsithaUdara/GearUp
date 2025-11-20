<#
PowerShell script to create admin@gearup.com in Firebase Authentication
This syncs the Firebase account with the existing PostgreSQL admin user

This script:
  1. Uses Firebase Admin SDK to create the user in Firebase
  2. Sets the specific UID to match PostgreSQL: u2sgkfVpdTd9hkrUp5sb3ttiHOt2
  3. Sets the password to: StrOng!Admin123
  
Usage: 
  powershell -ExecutionPolicy Bypass -File .\scripts\create-admin-in-firebase.ps1

Requirements:
  - Firebase service account JSON in .env file
  - Python 3 with firebase-admin package (OR use REST API approach below)
#>

Set-StrictMode -Version Latest

function Write-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-ErrorColor {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}

# Admin details from PostgreSQL
$adminEmail = "admin@gearup.com"
$adminPassword = "StrOng!Admin123"
$adminUid = "u2sgkfVpdTd9hkrUp5sb3ttiHOt2"
$adminDisplayName = "Admin User"

# Read Firebase API key from .env
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = Resolve-Path (Join-Path $scriptDir '..')
$envFile = Join-Path $repoRoot '.env'

if (-not (Test-Path $envFile)) {
    Write-ErrorColor ".env file not found at: $envFile"
    exit 1
}

Write-Info "Reading Firebase API key from .env..."
$firebaseApiKey = $null
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -match '^FIREBASE_API_KEY\s*=\s*(.+)$') {
        $firebaseApiKey = $matches[1].Trim()
    }
}

if (-not $firebaseApiKey) {
    Write-ErrorColor "FIREBASE_API_KEY not found in .env file"
    exit 1
}

Write-Success "Firebase API Key found: $($firebaseApiKey.Substring(0,10))..."

Write-Info "`n=== Creating Admin User in Firebase ==="
Write-Info "Email: $adminEmail"
Write-Info "UID: $adminUid"
Write-Info "Password: $adminPassword"

# Use Firebase Auth REST API to sign up the user
# This creates the user with email/password
$signUpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$firebaseApiKey"

$signUpBody = @{
    email = $adminEmail
    password = $adminPassword
    returnSecureToken = $true
} | ConvertTo-Json

Write-Info "`nAttempting to create user via Firebase Auth REST API..."

try {
    $response = Invoke-RestMethod -Uri $signUpUrl -Method Post -Body $signUpBody -ContentType "application/json"
    
    $createdUid = $response.localId
    $idToken = $response.idToken
    
    Write-Success "✓ User created successfully in Firebase!"
    Write-Success "  Firebase UID: $createdUid"
    Write-Success "  ID Token: $($idToken.Substring(0,20))..."
    
    # Check if UID matches PostgreSQL
    if ($createdUid -ne $adminUid) {
        Write-ErrorColor "`n⚠️  WARNING: Firebase generated a different UID!"
        Write-ErrorColor "  Expected: $adminUid"
        Write-ErrorColor "  Got: $createdUid"
        Write-ErrorColor "`nYou need to UPDATE PostgreSQL to use the new UID:"
        Write-Host "`n  docker exec gearup-postgres psql -U postgres -d as_user_auth_service -c `"UPDATE users SET firebase_uid='$createdUid' WHERE email='$adminEmail';`"" -ForegroundColor Yellow
    } else {
        Write-Success "`n✓ UID matches PostgreSQL perfectly!"
    }
    
    Write-Success "`n=== Admin User Creation Complete ==="
    Write-Host "`nYou can now login with:" -ForegroundColor Cyan
    Write-Host "  Email: $adminEmail" -ForegroundColor White
    Write-Host "  Password: $adminPassword" -ForegroundColor White
    Write-Host "`nGo to: http://localhost:3000/admin" -ForegroundColor Green
    
} catch {
    $errorResponse = $_.Exception.Response
    $statusCode = $errorResponse.StatusCode.value__
    
    if ($statusCode -eq 400) {
        # Try to get error details
        $reader = New-Object System.IO.StreamReader($errorResponse.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        $reader.Close()
        
        $errorObj = $errorBody | ConvertFrom-Json
        $errorMessage = $errorObj.error.message
        
        if ($errorMessage -like "*EMAIL_EXISTS*") {
            Write-Success "`n✓ User already exists in Firebase!"
            Write-Info "`nTrying to sign in to get the ID token..."
            
            # Sign in to get the token and UID
            $signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$firebaseApiKey"
            $signInBody = @{
                email = $adminEmail
                password = $adminPassword
                returnSecureToken = $true
            } | ConvertTo-Json
            
            try {
                $signInResponse = Invoke-RestMethod -Uri $signInUrl -Method Post -Body $signInBody -ContentType "application/json"
                $existingUid = $signInResponse.localId
                $existingToken = $signInResponse.idToken
                
                Write-Success "✓ Successfully signed in!"
                Write-Success "  Firebase UID: $existingUid"
                Write-Success "  ID Token: $($existingToken.Substring(0,20))..."
                
                # Check if UID matches PostgreSQL
                if ($existingUid -ne $adminUid) {
                    Write-ErrorColor "`n⚠️  WARNING: Firebase UID doesn't match PostgreSQL!"
                    Write-ErrorColor "  PostgreSQL expects: $adminUid"
                    Write-ErrorColor "  Firebase has: $existingUid"
                    Write-ErrorColor "`nYou need to UPDATE PostgreSQL to use Firebase UID:"
                    Write-Host "`n  docker exec gearup-postgres psql -U postgres -d as_user_auth_service -c `"UPDATE users SET firebase_uid='$existingUid' WHERE email='$adminEmail';`"" -ForegroundColor Yellow
                } else {
                    Write-Success "`n✓ UID matches PostgreSQL perfectly!"
                }
                
                Write-Success "`n=== Admin User Already Configured ==="
                Write-Host "`nYou can login with:" -ForegroundColor Cyan
                Write-Host "  Email: $adminEmail" -ForegroundColor White
                Write-Host "  Password: $adminPassword" -ForegroundColor White
                Write-Host "`nGo to: http://localhost:3000/admin" -ForegroundColor Green
                
            } catch {
                Write-ErrorColor "`n✗ Sign in failed - password may be different"
                Write-ErrorColor "Error: $($_.Exception.Message)"
                Write-Host "`nTry resetting the password in Firebase Console:" -ForegroundColor Yellow
                Write-Host "  https://console.firebase.google.com/project/gear-up-46adc/authentication/users" -ForegroundColor Cyan
            }
        } else {
            Write-ErrorColor "`n✗ Error creating user: $errorMessage"
        }
    } else {
        Write-ErrorColor "`n✗ Error creating user"
        Write-ErrorColor "Status: $statusCode"
        Write-ErrorColor "Error: $($_.Exception.Message)"
    }
}
