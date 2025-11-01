API Gateway — quick notes

This module contains the Spring Cloud Gateway used as an edge for the microservices.

What it does

- Routes HTTP requests to internal services.
- Handles authentication filters and shared cross-cutting concerns.

How to run locally

1. Ensure Config Server and dependent services are running (config-server, auth).
2. From repo root:

```
.\mvnw.cmd -pl api-gateway -am -DskipTests package
docker compose -f deployment/docker/docker-compose.yml build api-gateway
docker compose -f deployment/docker/docker-compose.yml up -d api-gateway
```

Where to change routes

- Check `src/main/resources/application.yml` and the dynamic config in `config-repo/api-gateway.yml` for route definitions.
