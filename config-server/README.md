# Config Server

Spring Cloud Config Server providing centralized, Git-backed configuration management for all GearUp microservices.

## Purpose

- **Centralized Config**: Single source of truth (`config-repo/`)
- **Version Control**: Git-backed configuration files
- **Environment Profiles**: dev, staging, prod support
- **Dynamic Refresh**: Update configs without redeployment

## Quick Start

```powershell
# Run locally
cd config-server
..\mvnw.cmd spring-boot:run

# Run with Docker
docker compose -f deployment/docker/docker-compose.yml up -d config-server
```

**Port**: 8888  
**Test**: http://localhost:8888/notification-service/default

## Configuration Repository

Location: `config-repo/`

```
config-repo/
├── application.yml              # Global configuration
├── api-gateway.yml              # Service-specific
├── notification-service.yml
└── shared/
    └── logging.yml              # Shared configs
```

## Adding Configuration for New Services

### 1. Create Config File

`config-repo/your-service.yml`:

```yaml
server:
  port: 8084

spring:
  application:
    name: your-service

# Service-specific properties
your-service:
  feature:
    enabled: true
```

### 2. Configure Service

In `application.properties`:

```properties
spring.application.name=your-service
spring.config.import=optional:configserver:http://localhost:8888
```

### 3. Verify

```powershell
curl http://localhost:8888/your-service/default
```

## Environment Profiles

Create profile-specific configs:

- `your-service.yml` (default)
- `your-service-dev.yml`
- `your-service-prod.yml`

Activate: `spring.profiles.active=dev`

## Testing

```powershell
# Health check
curl http://localhost:8888/actuator/health

# Get service config
curl http://localhost:8888/notification-service/default
curl http://localhost:8888/notification-service/prod
```

## Key Features

- **Encryption**: Built-in support for sensitive properties
- **Hot Reload**: Use `@RefreshScope` + `/actuator/refresh`
- **Service Discovery**: Integrated with Eureka

## Troubleshooting

| Issue              | Solution                                 |
| ------------------ | ---------------------------------------- |
| Config not loading | Verify `spring.config.import` in service |
| File not found     | Check file exists in `config-repo/`      |
| Wrong values       | Verify profile name matches file suffix  |

See root `README.md` for complete documentation.
