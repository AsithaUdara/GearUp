# API Gateway - Tracking Service Route Configuration

## Overview

The tracking service has been added to the API Gateway routing configuration. All requests to `/api/tracking/**` will be routed through the gateway to the tracking service.

## Configuration Details

**Route ID:** `tracking-service-route`  
**Service Name:** `tracking-service` (registered with Eureka)  
**Path Pattern:** `/api/tracking/**`  
**Rate Limiting:** 20 requests/second (burst: 40)

## How It Works

1. **Client Request:** `GET http://localhost:8080/api/tracking/employee/emp-001/tasks`
2. **Gateway Routes:** Request is forwarded to `lb://tracking-service` (load-balanced)
3. **Service Receives:** `/api/tracking/employee/emp-001/tasks` (full path preserved)
4. **Controller Matches:** `@RequestMapping("/api/tracking")` handles the request

## Testing

### Prerequisites
1. API Gateway must be running on port 8080
2. Tracking service must be running and registered with Eureka
3. Eureka service discovery must be running

### Test Commands

```powershell
# Test via Gateway
Invoke-WebRequest -Uri "http://localhost:8080/api/tracking/employee/emp-001/tasks" -UseBasicParsing

# Or use the test script
.\scripts\test-gateway-tracking.ps1
```

### Direct Service Access (for comparison)
```powershell
# Direct access (bypassing gateway)
Invoke-WebRequest -Uri "http://localhost:8086/api/tracking/employee/emp-001/tasks" -UseBasicParsing
```

## Route Priority

Routes are matched in order:
1. `/api/users/**` → user-auth-service (most specific)
2. `/api/tracking/**` → tracking-service (specific)
3. `/api/**` → api-gateway (catch-all, lowest priority)

## Rate Limiting

- **Replenish Rate:** 20 requests per second
- **Burst Capacity:** 40 requests
- Uses Redis for rate limiting state

## Troubleshooting

### Service Not Found
- Check if tracking-service is registered with Eureka
- Verify service name matches: `tracking-service`
- Check Eureka dashboard: `http://localhost:8761`

### 404 Not Found
- Verify the path matches `/api/tracking/**`
- Check if tracking service controllers have correct `@RequestMapping` paths

### Rate Limit Exceeded
- Current limit: 20 req/sec
- Increase in `application.yml` if needed

## Related Files

- `api-gateway/src/main/resources/application.yml` - Gateway configuration
- `config-repo/tracking-service.yml` - Tracking service config
- `scripts/test-gateway-tracking.ps1` - Gateway routing test script

