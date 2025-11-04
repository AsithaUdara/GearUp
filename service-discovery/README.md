# Service Discovery (Eureka Server)

Netflix Eureka-based service registry providing service discovery and load balancing for GearUp microservices.

## Purpose

- **Service Registration**: All microservices auto-register on startup
- **Service Discovery**: Services find each other without hardcoded URLs
- **Load Balancing**: Client-side load balancing across service instances
- **Health Monitoring**: Tracks service health and availability
- **Failover**: Automatic detection of unhealthy instances

## Quick Start

```powershell
# Run locally
cd service-discovery
..\mvnw.cmd spring-boot:run

# Run with Docker
docker compose -f deployment/docker/docker-compose.yml up -d service-discovery
```

**Port**: 8761  
**Dashboard**: http://localhost:8761

## Configuration

### Server Setup

In `application.yml`:

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

### Register Your Service

#### 1. Add Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 2. Enable Discovery Client

```java
@SpringBootApplication
@EnableDiscoveryClient
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

#### 3. Configure Service

In `application.properties`:

```properties
spring.application.name=your-service
server.port=8084

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

#### 4. Verify Registration

- Start your service
- Visit http://localhost:8761
- Your service should appear under "Instances currently registered with Eureka"

## Using Service Discovery

### Call Another Service

```java
@Service
public class YourService {

    @Autowired
    private RestTemplate restTemplate;

    public String callOtherService() {
        // Use service name instead of URL
        String url = "http://notification-service/api/notifications";
        return restTemplate.getForObject(url, String.class);
    }
}
```

### Configure Load-Balanced RestTemplate

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // Enable client-side load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Using Feign Client (Alternative)

```java
@FeignClient("notification-service")
public interface NotificationClient {

    @GetMapping("/api/notifications/{id}")
    Notification getNotification(@PathVariable Long id);
}
```

## Dashboard

Access Eureka Dashboard at http://localhost:8761

**Information Displayed**:

- Registered instances
- Service status (UP/DOWN)
- Last heartbeat time
- Instance metadata
- IP addresses and ports

## Health Monitoring

### Heartbeat Configuration

Services send heartbeats every 30 seconds (default).

```properties
# Customize heartbeat
eureka.instance.lease-renewal-interval-in-seconds=10
eureka.instance.lease-expiration-duration-in-seconds=30
```

### Check Service Health

```powershell
# Via Eureka API
curl http://localhost:8761/eureka/apps/notification-service

# Response shows instance status
```

## Self-Preservation Mode

Eureka's self-preservation mode prevents mass de-registration during network issues.

**Disabled for local dev** (in `application.yml`):

```yaml
eureka:
  server:
    enable-self-preservation: false
```

**Enable for production**:

```yaml
eureka:
  server:
    enable-self-preservation: true
```

## Multiple Instances

### Run Multiple Service Instances

```powershell
# Instance 1
java -jar -Dserver.port=8081 notification-service.jar

# Instance 2
java -jar -Dserver.port=8091 notification-service.jar
```

Eureka will automatically load-balance between instances.

## Testing

### Check Eureka Server Health

```powershell
curl http://localhost:8761/actuator/health
```

### List All Registered Services

```powershell
curl http://localhost:8761/eureka/apps
```

### Get Specific Service Info

```powershell
curl http://localhost:8761/eureka/apps/NOTIFICATION-SERVICE
```

## Monitoring

- **Dashboard**: http://localhost:8761
- **Apps**: http://localhost:8761/eureka/apps
- **Health**: http://localhost:8761/actuator/health
- **Metrics**: http://localhost:8761/actuator/metrics

## Dependencies

```xml
<!-- Eureka Server -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## Architecture

```
Service Discovery (Eureka)
    ↓
Registered Services:
    ├─ api-gateway
    ├─ config-server
    ├─ notification-service
    ├─ automobile-service
    └─ your-service

Service Communication:
notification-service → Eureka → automobile-service
(Uses service name, Eureka resolves to IP:Port)
```

## Key Files

- `ServiceDiscoveryApplication.java`: Main class with `@EnableEurekaServer`
- `application.yml`: Eureka server configuration

## Troubleshooting

| Issue                   | Solution                                     |
| ----------------------- | -------------------------------------------- |
| Service not registering | Check `@EnableDiscoveryClient` annotation    |
| Wrong IP displayed      | Set `eureka.instance.prefer-ip-address=true` |
| Service shows DOWN      | Verify service health endpoint responding    |
| Connection refused      | Ensure Eureka server is running on 8761      |
| Instance not removed    | Check heartbeat/lease settings               |

## Best Practices

1. **Service Names**: Use lowercase, hyphen-separated names
2. **Health Endpoints**: Always implement `/actuator/health`
3. **Metadata**: Add custom metadata for service identification
4. **Security**: Secure Eureka in production
5. **Backup**: Run multiple Eureka instances in production

## Production Considerations

### High Availability

Run multiple Eureka servers:

```yaml
# eureka-1
eureka:
  client:
    service-url:
      defaultZone: http://eureka-2:8761/eureka/
```

### Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```yaml
spring:
  security:
    user:
      name: admin
      password: ${EUREKA_PASSWORD}
```

See root `README.md` for complete infrastructure documentation.
