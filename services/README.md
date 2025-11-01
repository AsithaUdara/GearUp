Services — How to create and maintain a microservice in this monorepo

This file is a hands-on guide for working with service modules under `services/`.

What is a service module?

- Each folder under `services/` (e.g. `automobile-service`, `notification-service`) is a Spring Boot microservice.
- Each module has its own `pom.xml`, source code under `src/main/java`, and a `Dockerfile` used for containerization.

Step-by-step: create a new microservice

1. Create folder `services/my-service`
2. Create `pom.xml` with parent set to the repo root parent pom. Use consistent artifactId and groupId patterns.
3. Add `src/main/java/.../MyServiceApplication.java` with Spring Boot main method.
4. Add `src/main/resources/application.yml` for service-specific settings. Use Spring Cloud Config placeholders for values that live in `config-repo/` when appropriate.
5. Add a `Dockerfile` following the pattern used in other services (multi-stage builder + runtime image). Example Dockerfile is in the Developer Guide.
6. Update root `pom.xml` if the root uses an explicit module list for reactor builds (some projects list modules explicitly).
7. Add a configuration YAML to `config-repo/` (so Config Server can serve it during local runs). Example: `config-repo/my-service.yml`.
8. Add the service to `deployment/docker/docker-compose.yml` (copy an existing service block and adjust environment and volumes). Use the same network settings and depends_on appropriate supporting services (db, rabbitmq, etc.).
9. If the service should be reachable via API gateway, add a proxy route in `api-gateway` (see `api-gateway/README-LOCAL.md` for local gateway setup).
10. Add unit tests and integration tests, then run `mvn -DskipTests=false test` locally.

Build & test locally

- Package only this module in the reactor (from repo root):

```powershell
.\mvnw.cmd -pl services/my-service -am -DskipTests package
```

- Build Docker image for the service using the repo's production compose file (recommended):

```powershell
docker compose -f deployment/docker/docker-compose.yml build my-service --progress=plain
```

Debugging tips & gotchas

- When copying files into images, choose the correct relative path. Docker build contexts are set in `deployment/docker/docker-compose.yml`.
- If `./mvnw` fails inside a builder stage, ensure `mvnw` is executable (on Linux images use `RUN chmod +x ./mvnw` or call `sh ./mvnw`).
- Do not store secrets in `src/main/resources` or commit them to Git. Use `.env` and Docker secrets for local dev; for production use a vault solution.

Conventions

- Package names: `com.gearup.<service>`
- Logging: use the shared logging config in `config-repo/shared/logging.yml` where possible.
- Shared libraries: prefer `shared-libs/*` for reusable DTOs, auth code and event models.

## Database migrations

If your service needs a relational schema, place Flyway SQL migrations under `src/main/resources/db/migration`.

- Follow Flyway naming: `V1__init.sql`, `V2__...`.
- The repo includes a helper script and compose file to run Postgres and apply migrations: see `docs/POSTGRES_AND_MIGRATIONS.md` for step-by-step instructions (`scripts/run-flyway-locally.ps1` and `deployment/postgres/docker-compose.yml`).

If you want, I can create a service template skeleton under `services/template-service/` to speed up adding new services. Say "create template" and I'll add one.
