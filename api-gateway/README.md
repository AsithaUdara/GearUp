# API Gateway

Spring Cloud Gateway service providing centralized routing, authentication, and cross-cutting concerns for GearUp microservices.

## Purpose

- **Single Entry Point**: All client requests route through port 8080
- **Authentication**: Firebase token validation before routing to services
- **Load Balancing**: Automatic distribution via Eureka service discovery
- **Rate Limiting**: Caffeine-based request throttling
- **Circuit Breaking**: Resilience4j integration ready

## Quick Start

### Run Locally

```powershell
# Requires: Config Server, Eureka running
cd api-gateway
..\mvnw.cmd spring-boot:run
```

### Run with Docker

```powershell
docker compose -f deployment/docker/docker-compose.yml up -d api-gateway
docker logs -f gearup-api-gateway
```

## Configuration

### Routes (`application.yml`)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: notification-service
          uri: lb://notification-service # Load-balanced via Eureka
          predicates:
            - Path=/api/notification-service/**
          filters:
            - StripPrefix=2 # Remove /api/notification-service
            - name: FirebaseAuth # Apply authentication
```

## Adding Routes for New Services

### 1. Add Route Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: your-service
          uri: lb://your-service # Must match service name in Eureka
          predicates:
            - Path=/api/your-service/**
          filters:
            - StripPrefix=2
            - name: FirebaseAuth # Add if authentication required
```

### 2. Verify Service Registration

- Ensure your service has `@EnableDiscoveryClient`
- Check Eureka dashboard: http://localhost:8761
- Service must appear as registered

### 3. Test Route

```powershell
# Public endpoint
curl http://localhost:8080/api/your-service/public/hello

# Secured endpoint
curl -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
     http://localhost:8080/api/your-service/secure/data
```

## Authentication

### Firebase Integration

**User Info Injection**:
After successful authentication, gateway adds headers to downstream requests:

- `X-User-Email`: User's email
- `X-User-UID`: Firebase user ID
- `X-User-Name`: User's display name

Services can read these headers to identify the authenticated user.

**Public Routes** (no auth required):

```yaml
- Path=/api/public/**
```

**Secured Routes** (auth required):

```yaml
- name: FirebaseAuth
```

## Rate Limiting

Caffeine cache-based rate limiting configured in `RateLimitConfig.java`:

```java
@Bean
public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(5));
}
```

## Testing

```powershell
# Health check
curl http://localhost:8080/actuator/health

# Test routes
curl http://localhost:8080/api/notification-service/actuator/health
curl http://localhost:9090/api/template-service/actuator/health

# Authentication test (should fail without token)
curl http://localhost:8080/api/notification-service/secure/endpoint
```

## Key Files

- `application.yml`: Routes and gateway configuration
- `FirebaseGatewayFilter.java`: Authentication filter
- `RateLimitConfig.java`: Rate limiting setup
- `ApiGatewayApplication.java`: Main application class

## Monitoring

- **Actuator**: http://localhost:8080/actuator
- **Health**: http://localhost:8080/actuator/health
- **Routes**: http://localhost:8080/actuator/gateway/routes

## Troubleshooting

| Issue              | Solution                                                      |
| ------------------ | ------------------------------------------------------------- |
| Routes not working | Check service is registered in Eureka (http://localhost:8761) |
| 401 Unauthorized   | Verify Firebase credentials in `.env`                         |
| Service not found  | Ensure `uri: lb://service-name` matches Eureka registration   |
| Connection refused | Ensure target service is running and healthy                  |

See root `README.md` for complete infrastructure documentation.
