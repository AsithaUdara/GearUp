## Makefile helpers for running Flyway migrations locally (Linux/macOS)

.PHONY: flyway-notification flyway-user-auth docker-run-postgres docker-stop-postgres

docker-run-postgres:
	@if [ -f .env ]; then ENV_FILE="--env-file .env"; else ENV_FILE=""; fi; \
	docker run --name local_postgres $$ENV_FILE -e POSTGRES_DB=${POSTGRES_DB:-as_notification} -e POSTGRES_USER=${POSTGRES_USER:-svc_notification} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-${SPRING_DATASOURCE_PASSWORD}} -p 5432:5432 -d postgres:15

docker-stop-postgres:
	docker stop local_postgres || true && docker rm local_postgres || true

flyway-notification: docker-run-postgres
	@echo "Waiting for Postgres..."
	until pg_isready -h localhost -p 5432 -U ${POSTGRES_USER:-svc_notification}; do sleep 1; done
	./mvnw -pl services/notification-service flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/${POSTGRES_DB:-as_notification} -Dflyway.user=${POSTGRES_USER:-svc_notification} -Dflyway.password=${POSTGRES_PASSWORD:-${SPRING_DATASOURCE_PASSWORD}}

flyway-user-auth: docker-run-postgres
	@echo "Waiting for Postgres..."
	until pg_isready -h localhost -p 5432 -U ${POSTGRES_USER:-svc_user}; do sleep 1; done
	./mvnw -pl services/user-auth-service flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/${POSTGRES_DB:-as_user} -Dflyway.user=${POSTGRES_USER:-svc_user} -Dflyway.password=${POSTGRES_PASSWORD:-${SPRING_DATASOURCE_PASSWORD}}
