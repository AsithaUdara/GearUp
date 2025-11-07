# Test Chatbot API Access
# Run this script to test if chatbot is accessible without authentication

Write-Host "Testing GearUp Chatbot API..." -ForegroundColor Cyan
Write-Host ""

# Configuration
$apiGatewayUrl = "http://localhost:8080"
$chatEndpoint = "$apiGatewayUrl/api/chat/send"
$healthEndpoint = "$apiGatewayUrl/api/chat/health"

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
Write-Host "GET $healthEndpoint"
try {
    $healthResponse = Invoke-RestMethod -Uri $healthEndpoint -Method Get -ContentType "application/json"
    Write-Host "✓ Health check passed" -ForegroundColor Green
    Write-Host "Response: $($healthResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Send Message (No Auth)
Write-Host "Test 2: Send Message Without Authentication" -ForegroundColor Yellow
Write-Host "POST $chatEndpoint"

$testMessage = @{
    content = "Hello, I need help with my car"
    sessionId = "test-session-$(Get-Date -Format 'yyyyMMddHHmmss')"
} | ConvertTo-Json

Write-Host "Request Body:" -ForegroundColor Gray
Write-Host $testMessage -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $chatEndpoint -Method Post -Body $testMessage -ContentType "application/json"
    Write-Host "✓ Message sent successfully!" -ForegroundColor Green
    Write-Host "Bot Response:" -ForegroundColor Gray
    Write-Host "  Content: $($response.content)" -ForegroundColor White
    Write-Host "  Session ID: $($response.sessionId)" -ForegroundColor Gray
    Write-Host "  Intent: $($response.intent)" -ForegroundColor Gray
    if ($response.suggestedActions) {
        Write-Host "  Suggested Actions: $($response.suggestedActions -join ', ')" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Message send failed: $($_.Exception.Message)" -ForegroundColor Red
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Status Code: $statusCode" -ForegroundColor Red
    
    if ($statusCode -eq 403) {
        Write-Host ""
        Write-Host "403 FORBIDDEN - Authentication Required" -ForegroundColor Red
        Write-Host "This means the API Gateway is blocking unauthenticated requests." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Solutions:" -ForegroundColor Cyan
        Write-Host "1. Configure API Gateway to allow public access to /api/chat/**" -ForegroundColor White
        Write-Host "2. See: GearUp-backend\docs\CHATBOT_PUBLIC_ACCESS.md" -ForegroundColor White
        Write-Host "3. Add this to your SecurityConfig.java:" -ForegroundColor White
        Write-Host "   .pathMatchers(`"/api/chat/**`").permitAll()" -ForegroundColor Gray
        Write-Host ""
    } elseif ($statusCode -eq 404) {
        Write-Host ""
        Write-Host "404 NOT FOUND - Route Not Configured" -ForegroundColor Red
        Write-Host "The API Gateway route is not configured correctly." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Solutions:" -ForegroundColor Cyan
        Write-Host "1. Check api-gateway.yml route configuration" -ForegroundColor White
        Write-Host "2. Ensure chatbot-service is registered in Eureka" -ForegroundColor White
        Write-Host "3. Visit http://localhost:8761 to check service registration" -ForegroundColor White
        Write-Host ""
    }
}
Write-Host ""

# Test 3: CORS Preflight
Write-Host "Test 3: CORS Preflight Check" -ForegroundColor Yellow
Write-Host "OPTIONS $chatEndpoint"
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "content-type"
    }
    $corsResponse = Invoke-WebRequest -Uri $chatEndpoint -Method Options -Headers $headers
    
    if ($corsResponse.Headers["Access-Control-Allow-Origin"]) {
        Write-Host "✓ CORS configured correctly" -ForegroundColor Green
        Write-Host "  Allowed Origin: $($corsResponse.Headers['Access-Control-Allow-Origin'])" -ForegroundColor Gray
        Write-Host "  Allowed Methods: $($corsResponse.Headers['Access-Control-Allow-Methods'])" -ForegroundColor Gray
    } else {
        Write-Host "✗ CORS headers missing" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ CORS preflight failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If all tests passed:" -ForegroundColor Green
Write-Host "  ✓ Your chatbot is ready to use!" -ForegroundColor Green
Write-Host "  ✓ Frontend can connect without authentication" -ForegroundColor Green
Write-Host ""
Write-Host "If tests failed:" -ForegroundColor Yellow
Write-Host "  1. Check if all services are running (Eureka, API Gateway, Chatbot Service)" -ForegroundColor White
Write-Host "  2. Review the error messages above" -ForegroundColor White
Write-Host "  3. See docs/CHATBOT_PUBLIC_ACCESS.md for configuration help" -ForegroundColor White
Write-Host "  4. Check CHATBOT_QUICK_START.md for troubleshooting" -ForegroundColor White
Write-Host ""

# Service Status Check
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Service Status Check" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check Eureka
Write-Host "Eureka Service Discovery: " -NoNewline
try {
    $eurekaResponse = Invoke-RestMethod -Uri "http://localhost:8761/actuator/health" -Method Get -TimeoutSec 2
    Write-Host "✓ Running" -ForegroundColor Green
} catch {
    Write-Host "✗ Not accessible" -ForegroundColor Red
}

# Check API Gateway
Write-Host "API Gateway: " -NoNewline
try {
    $gatewayResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 2
    Write-Host "✓ Running" -ForegroundColor Green
} catch {
    Write-Host "✗ Not accessible" -ForegroundColor Red
}

# Check Chatbot Service (via Eureka)
Write-Host "Chatbot Service Registration: " -NoNewline
try {
    $eurekaApps = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps/CHATBOT-SERVICE" -Method Get -TimeoutSec 2
    Write-Host "✓ Registered" -ForegroundColor Green
} catch {
    Write-Host "✗ Not registered" -ForegroundColor Red
    Write-Host "  Make sure chatbot-service is running and registered with Eureka" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
