Template Service — skeleton

This folder is a minimal skeleton you can copy when creating a new microservice.

How to use

1. Copy this folder and rename it to `services/my-service`.
2. Edit `pom.xml`: change `artifactId`, `name`, and add dependencies your service needs.
3. Replace the package `com.gearup.templateservice` and class name with your service name.
4. Add `application.yml` under `src/main/resources` and put service-specific settings there.
5. Add the new module to the root reactor if your root POM lists modules explicitly.
6. Add a config YAML to `config-repo/` (optional) so Config Server can serve settings.

Quick build & run (from repo root):

```
.\mvnw.cmd -pl services/my-service -am -DskipTests package
docker compose -f deployment/docker/docker-compose.yml build my-service
docker compose -f deployment/docker/docker-compose.yml up -d my-service
```

Notes:

- Use the existing services as references for logging, config, and Dockerfile patterns.
- Do not commit secrets. Use `.env` or an external secrets manager.
