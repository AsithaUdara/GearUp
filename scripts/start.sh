#!/usr/bin/env bash
set -euo pipefail

# Resolve repo root based on this script's location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$REPO_ROOT/deployment/docker"
ENV_FILE="$REPO_ROOT/.env"
LOCAL_COMPOSE_ENV="$DOCKER_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERROR] .env not found at $ENV_FILE" >&2
  exit 1
fi

# Ensure deployment/docker/.env exists (compose services reference it)
if [[ ! -f "$LOCAL_COMPOSE_ENV" ]]; then
  echo "[INFO] Creating deployment docker .env from repo root .env ..."
  cp "$ENV_FILE" "$LOCAL_COMPOSE_ENV"
  {
    echo ""
    echo "# --- docker-level additions (explicit values for local dev) ---"
    echo "EUREKA_DEFAULT_ZONE=http://admin:password@service-discovery:8761/eureka"
    echo "FIREBASE_CONFIG_PATH=/app/secrets/firebase-service-account.json"
    echo "CHATBOT_DB_URL=jdbc:postgresql://db:5432/as_chatbot_service"
    echo "CHATBOT_DB_USER=svc_chatbot_service"
    echo "CHATBOT_DB_PASSWORD=postgres"
    echo "PGADMIN_EMAIL=admin@gearup.com"
    echo "PGADMIN_PASSWORD=admin123"
  } >> "$LOCAL_COMPOSE_ENV"
fi

cd "$DOCKER_DIR"
echo "[INFO] Starting backend services (compose up -d) using deployment/docker/.env for interpolation ..."
docker compose up -d

echo "[INFO] Current container status:"
docker compose ps
