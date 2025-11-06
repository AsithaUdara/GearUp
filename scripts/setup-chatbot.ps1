# GearUp Chatbot Service Setup Script
# This script sets up Ollama and pulls required models for the chatbot service

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "GearUp Chatbot Service Setup" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Check if Ollama is installed
Write-Host "Checking Ollama installation..." -ForegroundColor Yellow

# Try to find Ollama in common installation paths
$ollamaPath = $null
$possiblePaths = @(
    "$env:LOCALAPPDATA\Programs\Ollama\ollama.exe",
    "$env:ProgramFiles\Ollama\ollama.exe",
    "$env:APPDATA\Programs\Ollama\ollama.exe"
)

# Check common paths first
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $ollamaPath = $path
        break
    }
}

# If not found in common paths, try Get-Command
if (-not $ollamaPath) {
    $ollamaCmd = Get-Command ollama -ErrorAction SilentlyContinue
    if ($ollamaCmd) {
        $ollamaPath = $ollamaCmd.Source
    }
}

if (-not $ollamaPath) {
    Write-Host "Ollama is not installed or not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Ollama from: https://ollama.ai/download/windows" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "After installation, restart your terminal and run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Ollama found at: $ollamaPath" -ForegroundColor Green
Write-Host ""

# Check if Ollama service is running
Write-Host "Checking if Ollama service is running..." -ForegroundColor Yellow
try {
    $null = Invoke-WebRequest -Uri "http://localhost:11434/api/tags" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "[OK] Ollama service is running" -ForegroundColor Green
}
catch {
    Write-Host "Ollama service is not running. Starting Ollama..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Ollama will start in the background. This window will continue with setup." -ForegroundColor Gray
    
    # Start Ollama in background
    Start-Process -FilePath $ollamaPath -ArgumentList "serve" -WindowStyle Hidden
    
    # Wait for Ollama to start
    $maxAttempts = 10
    $attempt = 0
    $started = $false
    
    while ($attempt -lt $maxAttempts -and -not $started) {
        Start-Sleep -Seconds 2
        try {
            $null = Invoke-WebRequest -Uri "http://localhost:11434/api/tags" -Method Get -TimeoutSec 2 -ErrorAction Stop
            $started = $true
            Write-Host "[OK] Ollama service started successfully" -ForegroundColor Green
        }
        catch {
            $attempt++
            Write-Host "  Waiting for Ollama to start... ($attempt/$maxAttempts)" -ForegroundColor Gray
        }
    }
    
    if (-not $started) {
        Write-Host ""
        Write-Host "[ERROR] Failed to start Ollama service" -ForegroundColor Red
        Write-Host "Please start Ollama manually by running: ollama serve" -ForegroundColor Yellow
        exit 1
    }
}
Write-Host ""

# Pull required models
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Pulling Required Models" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Pull chat model
Write-Host "Pulling chat model (llama3.2:3b)..." -ForegroundColor Yellow
Write-Host "This may take several minutes depending on your internet speed..." -ForegroundColor Gray
& $ollamaPath pull llama3.2:3b

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Successfully pulled llama3.2:3b" -ForegroundColor Green
}
else {
    Write-Host "[ERROR] Failed to pull llama3.2:3b" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Pull embedding model
Write-Host "Pulling embedding model (nomic-embed-text)..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Gray
& $ollamaPath pull nomic-embed-text

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Successfully pulled nomic-embed-text" -ForegroundColor Green
}
else {
    Write-Host "[ERROR] Failed to pull nomic-embed-text" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Verify models
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Verifying Installation" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Available models:" -ForegroundColor Yellow
& $ollamaPath list
Write-Host ""

# Test embedding model
Write-Host "Testing embedding model..." -ForegroundColor Yellow
try {
    $null = & $ollamaPath run nomic-embed-text "test" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Embedding model is working" -ForegroundColor Green
    }
    else {
        Write-Host "[WARNING] Embedding model test returned exit code $LASTEXITCODE" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "[WARNING] Embedding model test inconclusive: $_" -ForegroundColor Yellow
}
Write-Host ""

# Test chat model
Write-Host "Testing chat model..." -ForegroundColor Yellow
try {
    $null = & $ollamaPath run llama3.2:3b "Hello" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Chat model is working" -ForegroundColor Green
    }
    else {
        Write-Host "[WARNING] Chat model test returned exit code $LASTEXITCODE" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "[WARNING] Chat model test inconclusive: $_" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Ensure PostgreSQL is running with pgvector extension" -ForegroundColor White
Write-Host "2. Update .env file with chatbot service configuration" -ForegroundColor White
Write-Host "3. Run: docker-compose up chatbot-service" -ForegroundColor White
Write-Host ""
Write-Host "For more information, see services/chatbot-service/README.md" -ForegroundColor Gray
