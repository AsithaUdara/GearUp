## Makefile helpers for local postgres (kept for convenience).
## Note: Flyway-based local migrations were removed in favor of a centralized
## DB initialization script: deployment/postgres/init-db.sql
## Developers should edit that script to add service schemas/tables and then
## bring up Postgres with Docker Compose (see README.md quick-start).

.PHONY: docker-run-postgres docker-stop-postgres

docker-run-postgres:
	@if [ -f .env ]; then ENV_FILE="--env-file .env"; else ENV_FILE=""; fi; \
	docker run --name local_postgres $$ENV_FILE -e POSTGRES_DB=${POSTGRES_DB:-gearup} -e POSTGRES_USER=${POSTGRES_USER:-gearup} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-password} -p 5432:5432 -d postgres:15

docker-stop-postgres:
	docker stop local_postgres || true && docker rm local_postgres || true
