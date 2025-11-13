## Makefile helpers for Flyway migrations and local Postgres
## Each service has its own database and migration scripts managed by Flyway

.PHONY: docker-run-postgres docker-stop-postgres flyway-notification flyway-user-auth flyway-template flyway-customer flyway-vehicle flyway-payment flyway-analytical flyway-tracking flyway-parts flyway-appointment flyway-modification flyway-all

docker-run-postgres:
	@if [ -f .env ]; then ENV_FILE="--env-file .env"; else ENV_FILE=""; fi; \
	docker run --name local_postgres $$ENV_FILE -e SPRING_DATASOURCE_DB=${SPRING_DATASOURCE_DB:-gearup} -e SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-postgres} -e SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-postgres} -p 5432:5432 -d postgres:15

docker-stop-postgres:
	docker stop local_postgres || true && docker rm local_postgres || true

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

# Run Flyway migrations for payment-service
flyway-payment:
	@echo "Running Flyway migrations for payment-service..."
	./mvnw -pl services/payment-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_PAYMENT:-jdbc:postgresql://localhost:5432/as_payment_service} \
		-Dflyway.user=$${FLYWAY_USER_PAYMENT:-svc_payment_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_PAYMENT:-payment_pass_2024}

# Run Flyway migrations for analytical-service
flyway-analytical:
	@echo "Running Flyway migrations for analytical-service..."
	./mvnw -pl services/analytical-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_ANALYTICAL:-jdbc:postgresql://localhost:5432/as_analytical_service} \
		-Dflyway.user=$${FLYWAY_USER_ANALYTICAL:-svc_analytical_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_ANALYTICAL:-analytical_svc_pass_2024}

# Run Flyway migrations for tracking-service
flyway-tracking:
	@echo "Running Flyway migrations for tracking-service..."
	./mvnw -pl services/tracking-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_TRACKING:-jdbc:postgresql://localhost:5432/as_tracking_service} \
		-Dflyway.user=$${FLYWAY_USER_TRACKING:-svc_tracking_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_TRACKING:-tracking_svc_pass_2024}

# Run Flyway migrations for parts-service
flyway-parts:
	@echo "Running Flyway migrations for parts-service..."
	./mvnw -pl services/parts-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_PARTS:-jdbc:postgresql://localhost:5432/as_parts_service} \
		-Dflyway.user=$${FLYWAY_USER_PARTS:-svc_parts_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_PARTS:-parts_svc_pass_2024}

# Run Flyway migrations for appointment-service
flyway-appointment:
	@echo "Running Flyway migrations for appointment-service..."
	./mvnw -pl services/appointment-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_APPOINTMENT:-jdbc:postgresql://localhost:5432/as_appointment_service} \
		-Dflyway.user=$${FLYWAY_USER_APPOINTMENT:-svc_appointment_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_APPOINTMENT:-appointment_pass_2024}

# Run Flyway migrations for modification-service
flyway-modification:
	@echo "Running Flyway migrations for modification-service..."
	./mvnw -pl services/modification-service flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_MODIFICATION:-jdbc:postgresql://localhost:5432/as_modification_service} \
		-Dflyway.user=$${FLYWAY_USER_MODIFICATION:-svc_modification_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_MODIFICATION:-modification_pass_2024}

# Run all Flyway migrations
flyway-all: flyway-notification flyway-user-auth flyway-template flyway-customer flyway-vehicle flyway-payment flyway-analytical flyway-tracking flyway-parts flyway-appointment flyway-modification
	@echo "All migrations completed successfully!"

