# Get Firebase Token via REST API
# This script gets a Firebase ID token using email/password

param(
    [Parameter(Mandatory=$true)]
    [string]$Email,
    
    [Parameter(Mandatory=$true)]
    [string]$Password
)

# Firebase API Key from your frontend .env
$API_KEY = Read-Host "Enter your Firebase API Key (NEXT_PUBLIC_FIREBASE_API_KEY)"

Write-Host "Getting Firebase token for: $Email" -ForegroundColor Cyan

# Firebase Auth REST API endpoint
$url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"

$body = @{
    email = $Email
    password = $Password
    returnSecureToken = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json"
    
    Write-Host ""
    Write-Host "✓ Login successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "User Information:" -ForegroundColor Yellow
    Write-Host "  Email: $($response.email)" -ForegroundColor White
    Write-Host "  User ID: $($response.localId)" -ForegroundColor White
    Write-Host "  Token expires in: $($response.expiresIn) seconds" -ForegroundColor White
    Write-Host ""
    Write-Host "ID Token (copy this):" -ForegroundColor Yellow
    Write-Host "-----------------------------------------------------------" -ForegroundColor Gray
    Write-Host $response.idToken -ForegroundColor Cyan
    Write-Host "-----------------------------------------------------------" -ForegroundColor Gray
    Write-Host ""
    
    # Copy to clipboard
    $response.idToken | Set-Clipboard
    Write-Host "✓ Token copied to clipboard!" -ForegroundColor Green
    Write-Host ""
    
    return $response.idToken
} catch {
    Write-Host "✗ Login failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Details: $errorBody" -ForegroundColor Red
    }
}
