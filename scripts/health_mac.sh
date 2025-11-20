#!/usr/bin/env bash
# macOS-friendly health check script for GearUp services
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

print() { printf "%b\n" "$1"; }

HAS_CURL=1
command -v curl >/dev/null 2>&1 || HAS_CURL=0

if [ "$HAS_CURL" -eq 0 ]; then
  print "ERROR: 'curl' is required but not installed. Install via 'brew install curl' or use system curl." >&2
  exit 2
fi

# Default service -> port list (host ports mapped by docker-compose)
DEFAULTS=(
  "config-server:8888"
  "service-discovery:8761"
  "api-gateway:9090"
  "vehicle-service:8090"
  "modification-service:8089"
  "user-auth-service:8082"
  "tracking-service:8091"
  "analytical-service:8087"
  "customer-service:8088"
  "payment-service:8083"
  "parts-service:8093"
  "template-service:8085"
  "appointment-service:8084"
  "notification-service:8081"
  "chatbot-service:8086"
)

services_to_check=()
if [ "$#" -eq 0 ]; then
  services_to_check=("${DEFAULTS[@]}")
else
  # Accept either 'all' or a list of names or host:port entries
  if [ "$1" = "all" ]; then
    services_to_check=("${DEFAULTS[@]}")
  else
    for arg in "$@"; do
      # if arg contains colon, treat as host:port
      if [[ "$arg" == *":"* ]]; then
        services_to_check+=("$arg")
      else
        # try to find in defaults by prefix match
        found=0
        for d in "${DEFAULTS[@]}"; do
          name="${d%%:*}"
          if [ "$name" = "$arg" ]; then
            services_to_check+=("$d")
            found=1
            break
          fi
        done
        if [ $found -eq 0 ]; then
          print "Warning: unknown service '$arg' — treat as host:port required. Skipping." >&2
        fi
      fi
    done
  fi
fi

any_failed=0
printf "\nChecking %d endpoint(s)...\n\n" "${#services_to_check[@]}"

for s in "${services_to_check[@]}"; do
  host="${s%%:*}"
  port="${s##*:}"
  url="http://localhost:${port}/actuator/health"

  # curl with timeout and show HTTP status and body
  out=""
  status_code=0
  if out=$(curl -sS --max-time 5 -w "\n%{http_code}" "$url" 2>&1); then
    # split body and code
    body=$(printf "%s" "$out" | sed '$d')
    status_code=$(printf "%s" "$out" | tail -n1)
  else
    # curl failed; capture output and set non-200 code
    body="$(printf "%s" "$out")"
    status_code=000
  fi

  # extract 'status' field from JSON if possible
  health_status="UNKNOWN"
  if command -v jq >/dev/null 2>&1; then
    health_status=$(printf "%s" "$body" | jq -r '.status // empty' 2>/dev/null || true)
  else
    # fallback: look for "status" : "UP" / "DOWN"
    if printf "%s" "$body" | grep -iq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
      health_status="UP"
    elif printf "%s" "$body" | grep -iq '"status"[[:space:]]*:[[:space:]]*"DOWN"'; then
      health_status="DOWN"
    else
      # leave UNKNOWN
      :
    fi
  fi

  if [ "$status_code" = "200" ] && [ "$health_status" = "UP" ]; then
    printf "[32mOK[0m  %s -> %s (HTTP %s)\n" "$host" "$url" "$status_code"
  else
    any_failed=1
    printf "[31mFAIL[0m %s -> %s (HTTP %s) status=%s\n" "$host" "$url" "$status_code" "$health_status"
    # print a short tail of body for debugging
    printf "%s\n" "--- body (truncated) ---"
    printf "%s\n" "$(printf "%s" "$body" | tail -n 20)"
    printf "\n"
  fi
done

if [ $any_failed -ne 0 ]; then
  printf "One or more services are not healthy.\n" >&2
  exit 1
else
  printf "All checked services are UP.\n"
  exit 0
fi
