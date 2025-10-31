# Deployment / Docker (local dev)

This folder contains docker and deployment scaffolding.

- `deployment/docker/docker-compose.yml` - local compose file that builds from `services/*` contexts.
- `deployment/k8s/` - kubernetes manifests (create per-environment in future).
- `deployment/helm/` - helm charts for templated deployment.

Quick start (local):

1. Copy `.env.example` to `.env` and configure required secrets (Firebase service account, DB credentials, RabbitMQ creds, Redis host/port).
2. From repository root run (Docker Desktop required):

```powershell
# Build and start in detached mode
docker compose -f deployment/docker/docker-compose.yml up --build -d

# Tail logs
docker compose -f deployment/docker/docker-compose.yml logs -f

# Tear down
docker compose -f deployment/docker/docker-compose.yml down
```

Notes:

- For production use, replace docker-compose with Kubernetes + Helm charts in `deployment/helm`.
- Each service should provide its own Dockerfile at `services/<name>/Dockerfile`.
