# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a secure non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy only the prebuilt JAR from the host (ensure mvnw build is done before)
COPY services/customer-service/target/customer-service-*.jar app.jar

# Adjust ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8088

# Health check to verify application readiness
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8088/actuator/health || exit 1

# Metadata labels for observability
LABEL maintainer="ops@gearup.com" \
  org.opencontainers.image.version="1.0.0" \
  org.opencontainers.image.title="GearUp Customer Service" \
  org.opencontainers.image.description="Handles customer management and profile data for the GearUp platform"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
