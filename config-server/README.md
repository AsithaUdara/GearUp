Config Server — quick notes

This module is a Spring Cloud Config server that serves configuration YAMLs from `config-repo/`.

Local usage

1. Ensure `config-repo/` contains the YAML files required by each service (e.g., `notification-service.yml`).
2. Build and run:

```
.\mvnw.cmd -pl config-server -am -DskipTests package
docker compose -f deployment/docker/docker-compose.yml build config-server
docker compose -f deployment/docker/docker-compose.yml up -d config-server
```

Notes

- The Config Server reads files from the `config-repo/` folder in this repository in local setups. In production you may point it to a separate Git repository.
