#!/usr/bin/env bash
set -euo pipefail

HOST="localhost"
services=(
  "service-discovery:8761"
  "config-server:8888"
  "api-gateway:9090"
  "customer-service:8088"
  "vehicle-service:8090"
  "modification-service:8086"
  "user-auth-service:8082"
  "notification-service:8081"
  "automobile-service:8085"
  "chatbot-service:8086"
)

for svc in "${services[@]}"; do
  name="${svc%%:*}"
  port="${svc##*:}"
  url="http://$HOST:$port/actuator/health"
  echo -n "[CHECK] $name ($port): "
  if out="$(curl -s --max-time 5 "$url" 2>/dev/null)"; then
    if grep -q '"UP"' <<<"$out"; then
      echo "UP"
    else
      echo "UNEXPECTED RESPONSE: $out"
    fi
  else
    echo "UNREACHABLE"
  fi
done
