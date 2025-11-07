# SET_ENV_VARS.ps1
# Helper script to set environment variables for GearUp Backend
# 
# Usage:
#   1. Edit the values below with your actual credentials
#   2. Run this script: . .\SET_ENV_VARS.ps1
#   3. The variables will be set in your current PowerShell session
#
# NOTE: Variables set this way only last for the current PowerShell session.
# You need to run this script each time you open a new terminal.

Write-Host "╔═══════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "║     Setting GearUp Backend Environment Variables     ║" -ForegroundColor Yellow
Write-Host "║                                                       ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ============================================
# EDIT THESE VALUES WITH YOUR ACTUAL CREDENTIALS
# ============================================

# PostgreSQL Password
$env:POSTGRES_PASSWORD = "Niro"  # 👈 CHANGE THIS to your PostgreSQL password

# RabbitMQ Credentials
$env:RABBITMQ_USERNAME = "automobile_admin"
$env:RABBITMQ_PASSWORD = "123456"  # 👈 CHANGE THIS to your RabbitMQ password

# Optional: Spring Boot specific overrides
# $env:SPRING_DATASOURCE_USERNAME = "postgres"
# $env:SPRING_DATASOURCE_PASSWORD = $env:POSTGRES_PASSWORD

# ============================================
# Display what was set
# ============================================
Write-Host "✅ Environment variables set for current session:" -ForegroundColor Green
Write-Host ""
Write-Host "  POSTGRES_PASSWORD     = " -NoNewline -ForegroundColor White
Write-Host "********" -ForegroundColor Gray
Write-Host "  RABBITMQ_USERNAME     = $env:RABBITMQ_USERNAME" -ForegroundColor White
Write-Host "  RABBITMQ_PASSWORD     = " -NoNewline -ForegroundColor White
Write-Host "********" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 These variables are set for your current PowerShell session only." -ForegroundColor Yellow
Write-Host "   Run this script again if you open a new terminal." -ForegroundColor Yellow
Write-Host ""
Write-Host "🚀 You can now start the services:" -ForegroundColor Cyan
Write-Host "   .\scripts\start-all-services.ps1" -ForegroundColor White
Write-Host ""
