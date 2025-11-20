#!/usr/bin/env bash
# Grant required DB privileges for services that failed Flyway migrations
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PSQL_CONTAINER=gearup-postgres

declare -a TARGETS=(
  "as_parts_service:svc_parts_service"
  "as_template_service:svc_template_service"
  "as_appointment_service:svc_appointment_service"
)

for t in "${TARGETS[@]}"; do
  db=${t%%:*}
  user=${t##*:}
  echo "Applying grants for DB='$db' user='$user'..."
  docker exec "$PSQL_CONTAINER" psql -U postgres -d "$db" -c "GRANT ALL ON SCHEMA public TO \"$user\"; GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO \"$user\"; GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO \"$user\"; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO \"$user\"; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO \"$user\";"
  echo "Done for $db/$user." || true
done

echo "All grants applied. You may restart affected services now."
