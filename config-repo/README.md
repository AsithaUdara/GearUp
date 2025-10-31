# config-repo (GearUp)

This repository directory holds non-sensitive default configuration for the GearUp microservices. It is intended to be used by a Spring Cloud Config Server (see `../config-server`) and _must not_ contain production secrets.

## Structure

- `application.yml` — global defaults for all services (logging format, common timeouts, tracing tags).
- `bootstrap.yml` — bootstrap-time defaults used when clients contact the Config Server during startup.
- `<service>.yml` — service-specific defaults (datasource placeholders, redis, rabbitmq, eureka client defaultZone placeholders).
- `shared/` — shared fragments, e.g. `logging.yml` used by many services.

## Principles

- Do not put secrets (database passwords, API keys, private keys) in this git repo.
  - Use Kubernetes Secrets, SealedSecrets, or an external secrets manager (Vault, AWS Secrets Manager, Azure Key Vault) in production.
- Store only non-sensitive defaults and placeholders here.
- Use property placeholders so runtime values (hostname, credentials, secrets) are supplied by environment, k8s manifests, or CI/CD.

## Placeholders & environment overrides

- Use `${VAR_NAME:default}` style placeholders in these YAML files. Example:

  datasource:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
  username: ${DB_USER:}
  password: ${DB_PASSWORD:}

- Override values at deploy-time via environment variables, container `env`, or k8s `valueFrom` (Secrets/ConfigMaps). Example (k8s Deployment snippet):

  env:

  - name: DB_USER
    valueFrom:
    secretKeyRef:
    name: my-app-secret
    key: db.user

- The `config-server` app supports the same overrides through environment variables (we default the server to use `file:../config-repo` for local dev). Useful env vars used by `config-server/application.yml`:
  - `CONFIG_REPO_URI` — git URL or `file:` path to the config repo (default: `file:../config-repo`)
  - `CONFIG_SERVER_USER` / `CONFIG_SERVER_PASSWORD` — basic auth credentials for the config server
  - `CONFIG_SERVER_CORS_ORIGINS` — allowed origins (comma separated)

## Bootstrap example for clients

Put this minimal snippet in each client under `src/main/resources/bootstrap.yml` or configure it as part of the container environment so the client can locate the config server before the application context fully loads:

spring:
application:
name: my-service # should match the file name in config-repo (e.g. my-service.yml)
cloud:
config:
uri: ${CONFIG_SERVER_URI:http://config-server:8888} # label/profile options
label: ${CONFIG_LABEL:main}
profile: ${SPRING_PROFILES_ACTIVE:default}
fail-fast: ${CONFIG_FAIL_FAST:true}

Notes:

- `spring.application.name` is used to select the file served by the config server (for example `api-gateway` → `api-gateway.yml`).
- `fail-fast` ensures the client fails startup if it cannot reach the config server (helpful in k8s for detecting missing config at boot).

## Local development

- The `config-server` defaults to `file:../config-repo` so you can run config-server locally and it will serve files from this folder.
- Example: run `config-server` then hit `http://localhost:8888/{application}/{profile}` to verify configuration (for example `http://localhost:8888/api-gateway/default`).

## Security & encrypt/decrypt

- For sensitive properties you can:
  1. Keep them out of git and supply them via environment or k8s Secrets.
  2. Use Spring Cloud Config Server's symmetric/asymmetric encrypt/decrypt (requires a key pair that should _not_ be stored in git).
- If you enable encryption, add the keys to a secure store and configure the config-server to use them (see Spring Cloud Config docs).

## Kubernetes notes

- Option A (recommended): Host the config repo in a real Git server and give the config-server a read-only Git credential (stored as a k8s Secret). Mount or supply the credentials as env vars.
- Option B (simple): Build the config repo into a ConfigMap or mount it into the `config-server` pod as a volume (useful when you cannot reach an external Git server).

## Where to go next

- Add CI/CD steps to sync `config-repo` to your Git server if you keep the repo local for development.
- Add k8s manifests for `config-server` (Deployment, Service, Secret with git creds) so it can run in-cluster.
- If you want, I can add an example `k8s/` manifest and a short README section showing how to wire secrets for the config-server.
