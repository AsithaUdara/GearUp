# Environment Variables Consolidation Summary

## Overview

This document outlines the standardized environment variables used across the GearUp backend microservices. All sensitive variables have been consolidated and duplicates/inconsistencies have been resolved.

## Changes Made

### 1. **Standardized Variable Names**

#### PostgreSQL/Database Variables

- **Standardized**: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- **Removed**: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` (from service configs)
- **Kept in .env**: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` (for Docker container setup only)
- Each service now has its own dedicated database credentials (e.g., `TEMPLATE_DB_URL`, `TEMPLATE_DB_USER`, `TEMPLATE_DB_PASSWORD`)

#### Redis Variables

- **Standardized**: `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `SPRING_REDIS_PASSWORD`
- **Deprecated**: `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `REDIS_HOST`, `REDIS_PORT`
- **Note**: Both old and new variable names are supported in .env files for backward compatibility during migration

#### RabbitMQ Variables

- **Standardized**: `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
- **Deprecated**: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- **Note**: Both old and new variable names are supported in .env files for backward compatibility

#### Eureka Service Discovery

- **Standardized**: `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `EUREKA_ADMIN_USER`, `EUREKA_ADMIN_PASSWORD`
- **Deprecated**: `EUREKA_DEFAULT_ZONE`, `EUREKA_URL`
- **Note**: Both old and new variable names are supported in .env files for backward compatibility

#### Config Server

- **Standardized**: `SPRING_CLOUD_CONFIG_URI`, `CONFIG_SERVER_USER`, `CONFIG_SERVER_PASSWORD`
- **Removed duplicates**: Consolidated from multiple variations

### 2. **Service-Specific Database Configuration**

Each microservice now has its own set of database variables following this pattern:

```
{SERVICE}_DB_URL=jdbc:postgresql://{HOST}:{PORT}/as_{service}_service
{SERVICE}_DB_USER=svc_{service}_service
{SERVICE}_DB_PASSWORD={unique_password}
```

**Services with database configuration:**

- notification-service
- user-auth-service
- chatbot-service
- vehicle-service
- customer-service
- tracking-service
- analytical-service
- payment-service
- parts-service
- appointment-service
- modification-service
- template-service

### 3. **Docker vs Local Development**

#### Docker Environment (deployment/docker/.env)

- Database host: `db` (Docker service name)
- Redis host: `redis`
- RabbitMQ host: `rabbitmq`
- Config Server: `http://config-server:8888`
- Eureka: `http://admin:password@service-discovery:8761/eureka/`

#### Local Development (root .env)

- Database host: `localhost`
- Redis host: `localhost`
- RabbitMQ host: `localhost`
- Config Server: `http://localhost:8888`
- Eureka: `http://admin:password@localhost:8761/eureka`

### 4. **Security Improvements**

1. All default passwords changed to placeholder values (`change_me_*`)
2. No hardcoded passwords in configuration files
3. All sensitive values externalized to environment variables
4. Firebase credentials path externalized
5. Config server credentials externalized

### 5. **Files Modified**

#### Environment Configuration Files

- `/.env.example` - Root environment template (for local development)
- `/deployment/docker/.env.example` - Docker environment template

#### Service Configuration Files (config-repo)

- `template-service.yml`
- `chatbot-service.yml`
- All other service YML files standardized

#### Individual Service Application Files

- `services/user-auth-service/src/main/resources/application.yml`
- `services/vehicle-service/src/main/resources/application.yml`
- `services/customer-service/src/main/resources/application.yml`
- `services/tracking-service/src/main/resources/application.yml`
- `services/analytical-service/src/main/resources/application.yml`

#### Infrastructure Files

- `api-gateway/src/main/resources/application.yml`
- `service-discovery/src/main/resources/application.yml`
- `deployment/docker/docker-compose.yml`

## Environment Variable Reference

### Core Infrastructure

#### PostgreSQL Master

```bash
POSTGRES_HOST=db                    # Docker: db, Local: localhost
POSTGRES_PORT=5432
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_me_postgres_password
```

#### Redis

```bash
SPRING_DATA_REDIS_HOST=redis        # Docker: redis, Local: localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=              # Optional, empty for no auth
```

#### RabbitMQ

```bash
SPRING_RABBITMQ_HOST=rabbitmq       # Docker: rabbitmq, Local: localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=change_me_rabbitmq_password
```

#### Eureka Service Discovery

```bash
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://admin:change_me_eureka_password@service-discovery:8761/eureka/
EUREKA_ADMIN_USER=admin
EUREKA_ADMIN_PASSWORD=change_me_eureka_password
```

#### Config Server

```bash
SPRING_CLOUD_CONFIG_URI=http://config-server:8888
CONFIG_SERVER_USER=configadmin
CONFIG_SERVER_PASSWORD=change_me_config_password
CONFIG_REPO_URI=file:/app/config-repo  # Docker path
```

### Service-Specific Databases

Each service follows this pattern (example for template-service):

```bash
TEMPLATE_DB_URL=jdbc:postgresql://db:5432/as_template_service
TEMPLATE_DB_USER=svc_template_service
TEMPLATE_DB_PASSWORD=change_me_template_password
```

### Firebase

```bash
FIREBASE_SERVICE_ACCOUNT_JSON=/app/secrets/firebase-service-account.json
FIREBASE_CONFIG_PATH=/app/secrets/firebase-service-account.json
APP_FIREBASE_CONFIGURATION_FILE=/app/secrets/firebase-service-account.json
```

### Ollama (Chatbot AI)

```bash
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_CHAT_MODEL=llama3.2:3b
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
```

### Additional Configuration

```bash
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SMTP_HOST=localhost
SMTP_PORT=25
WEBSOCKET_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

## Usage Instructions

### For Docker Deployment

1. Copy the template:

   ```bash
   cd deployment/docker
   Copy-Item .env.example .env
   ```

2. Edit `.env` and replace all `change_me_*` passwords with secure values

3. Start the services:
   ```bash
   docker-compose up -d
   ```

### For Local Development

1. Copy the template:

   ```bash
   Copy-Item .env.example .env
   ```

2. Edit `.env` and:

   - Replace all `change_me_*` passwords
   - Update hosts to `localhost` where needed
   - Update Firebase path to your local path

3. Start services individually or use your IDE

## Fallback Values

All configuration files include fallback values for development. These are defined using the pattern:

```yaml
property: ${ENV_VAR:fallback_value}
```

For example:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/as_service_name}
```

This ensures services can start even if environment variables aren't set, though it's **not recommended for production**.

## Migration Checklist

- [x] Consolidated duplicate variables
- [x] Standardized naming conventions
- [x] Removed hardcoded credentials
- [x] Created comprehensive .env.example files
- [x] Updated all service configuration files
- [x] Updated docker-compose.yml
- [x] Added fallback values for development
- [x] Documented all changes

## Security Best Practices

1. **Never commit .env files** - They are in .gitignore
2. **Use strong passwords** - Replace all `change_me_*` values
3. **Rotate credentials regularly** - Especially for production
4. **Use secrets management** - Consider using Docker secrets or Kubernetes secrets for production
5. **Limit access** - Only give services the database credentials they need
6. **Enable authentication** - Configure Redis password in production
7. **Use TLS/SSL** - Enable secure connections for all external communication

## Troubleshooting

### Services can't connect to database

- Verify `SPRING_DATASOURCE_URL` points to correct host (`db` for Docker, `localhost` for local)
- Check database credentials match what's in the `.env` file
- Ensure database initialization script created the service-specific databases

### Services can't connect to Redis

- Verify `SPRING_DATA_REDIS_HOST` is set correctly
- Check if Redis requires password authentication
- Ensure Redis container/service is running

### Config Server not loading properties

- Verify `SPRING_CLOUD_CONFIG_URI` points to correct Config Server
- Check Config Server credentials if authentication is enabled
- Ensure config-repo directory is mounted/accessible

### Eureka registration failing

- Verify `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` is correct
- Check Eureka admin credentials
- Ensure Eureka service is healthy before starting dependent services

## Additional Notes

- The `SPRING_DATA_REDIS_*` prefix is the standard Spring Boot 2.x+ convention
- Older variable names are kept in .env for backward compatibility but should be phased out
- All Docker services use internal Docker network names (e.g., `db`, `redis`, `rabbitmq`)
- Local development uses `localhost` for all services
- Firebase credentials should be mounted as read-only volumes in Docker
