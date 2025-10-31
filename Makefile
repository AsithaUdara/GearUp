## Makefile helpers for running Flyway migrations locally (Linux/macOS)

.PHONY: flyway-notification flyway-user-auth docker-run-postgres docker-stop-postgres

docker-run-postgres:
	docker run --name local_postgres -e POSTGRES_DB=as_notification -e POSTGRES_USER=svc_notification -e POSTGRES_PASSWORD=changeme -p 5432:5432 -d postgres:15

docker-stop-postgres:
	docker stop local_postgres || true && docker rm local_postgres || true

flyway-notification: docker-run-postgres
	@echo "Waiting for Postgres..."
	until pg_isready -h localhost -p 5432 -U svc_notification; do sleep 1; done
	./mvnw -pl services/notification-service flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/as_notification -Dflyway.user=svc_notification -Dflyway.password=changeme

flyway-user-auth: docker-run-postgres
	@echo "Waiting for Postgres..."
	until pg_isready -h localhost -p 5432 -U svc_user; do sleep 1; done
	./mvnw -pl services/user-auth-service flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/as_user -Dflyway.user=svc_user -Dflyway.password=changeme
