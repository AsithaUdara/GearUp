# Containerization and Image Registry Guide

This repository uses multi-stage Dockerfiles and Helm charts to build and deploy microservices.

Key conventions

- Image naming: registry.company.com/as/<service>:<version>-<build>
- Ports: services expose 8080 unless otherwise noted.
- Health: services must expose `/actuator/health`.

Building locally (example)

1. Build with Maven and package:

   ./mvnw -DskipTests package

2. Build image (example for notification-service):

   docker build -f services/notification-service/Dockerfile -t registry.company.com/as/notification-service:1.0.0-local ./

3. Run locally:

   docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=local registry.company.com/as/notification-service:1.0.0-local

CI requirements

- Provide registry credentials as secrets in CI: REGISTRY_USERNAME and REGISTRY_PASSWORD (or DOCKERHUB_TOKEN). Update workflows in `.github/workflows` to reference your registry and credentials.

Required CI secrets for the provided GitHub Actions workflow

- REGISTRY_URL: the registry host (e.g. registry.company.com)
- REGISTRY_USERNAME: username for container registry
- REGISTRY_PASSWORD: password/token for container registry
- KUBE_CONFIG_BASE64: base64-encoded kubeconfig for the cluster where Helm will deploy (keep private)

Optional environment

- HELM_NAMESPACE: (workflow default is `staging`) you can set this in the workflow or provide as a repo-level environment variable.

Helm

- Charts live under `deployment/helm/charts/<service>`.
- Use `helm lint` and `helm template` to validate charts before deploying.

Security

- Do not commit secrets in repository. Use sealed secrets or external secret managers.
