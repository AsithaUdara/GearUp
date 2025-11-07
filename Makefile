## Makefile helpers for Flyway migrations and local Postgres
## Each service has its own database and migration scripts managed by Flyway

.PHONY: docker-run-postgres docker-stop-postgres flyway-automobile flyway-notification flyway-user-auth flyway-template flyway-customer flyway-vehicle flyway-all

docker-run-postgres:
	@if [ -f .env ]; then ENV_FILE="--env-file .env"; else ENV_FILE=""; fi; \
	docker run --name local_postgres $$ENV_FILE -e POSTGRES_DB=${POSTGRES_DB:-gearup} -e POSTGRES_USER=${POSTGRES_USER:-gearup} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-password} -p 5432:5432 -d postgres:15

docker-stop-postgres:
	docker stop local_postgres || true && docker rm local_postgres || true

# Run Flyway migrations for automobile-service
flyway-automobile:
	@echo "Running Flyway migrations for automobile-service..."
	./mvnw -pl services/automobile-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_AUTO:-jdbc:postgresql://localhost:5432/as_automobile_service} \
		-Dflyway.user=$${FLYWAY_USER_AUTO:-svc_automobile_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_AUTO:-auto_svc_pass_2024}

# Run Flyway migrations for notification-service
flyway-notification:
	@echo "Running Flyway migrations for notification-service..."
	./mvnw -pl services/notification-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_NOTIF:-jdbc:postgresql://localhost:5432/as_notification_service} \
		-Dflyway.user=$${FLYWAY_USER_NOTIF:-svc_notification_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_NOTIF:-notif_svc_pass_2024}

# Run Flyway migrations for user-auth-service
flyway-user-auth:
	@echo "Running Flyway migrations for user-auth-service..."
	./mvnw -pl services/user-auth-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_AUTH:-jdbc:postgresql://localhost:5432/as_user_auth_service} \
		-Dflyway.user=$${FLYWAY_USER_AUTH:-svc_user_auth_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_AUTH:-auth_svc_pass_2024}

# Run Flyway migrations for template-service
flyway-template:
	@echo "Running Flyway migrations for template-service..."
	./mvnw -pl services/template-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_TEMPLATE:-jdbc:postgresql://localhost:5432/as_template_service} \
		-Dflyway.user=$${FLYWAY_USER_TEMPLATE:-svc_template_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_TEMPLATE:-template_svc_pass_2024}

# Run Flyway migrations for customer-service
flyway-customer:
	@echo "Running Flyway migrations for customer-service..."
	./mvnw -pl services/customer-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_CUSTOMER:-jdbc:postgresql://localhost:5432/as_customer_service} \
		-Dflyway.user=$${FLYWAY_USER_CUSTOMER:-svc_customer_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_CUSTOMER:-customer_svc_pass_2024}

# Run Flyway migrations for vehicle-service
flyway-vehicle:
	@echo "Running Flyway migrations for vehicle-service..."
	./mvnw -pl services/vehicle-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_VEHICLE:-jdbc:postgresql://localhost:5432/as_vehicle_service} \
		-Dflyway.user=$${FLYWAY_USER_VEHICLE:-svc_vehicle_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_VEHICLE:-vehicle_svc_pass_2024}

# Run all Flyway migrations
flyway-all: flyway-automobile flyway-notification flyway-user-auth flyway-template flyway-customer flyway-vehicle
	@echo "All migrations completed successfully!"

