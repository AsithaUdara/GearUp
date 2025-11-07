#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$REPO_ROOT/deployment/docker"
ENV_FILE="$REPO_ROOT/.env"

cd "$DOCKER_DIR"
PURGE="${1:-}"

if [[ "$PURGE" == "--purge" ]]; then
  echo "[INFO] Stopping and removing containers, networks, and volumes ..."
  docker compose down -v
else
  echo "[INFO] Stopping and removing containers and networks ..."
  docker compose down
fi
