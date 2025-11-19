# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy only the prebuilt JAR (ensure it's built before this step)
COPY services/appointment-service/target/appointment-service-*.jar app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose application port
EXPOSE 8084

# Add a lightweight health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Metadata labels
LABEL maintainer="ops@gearup.com" \
  org.opencontainers.image.version="1.0.0" \
  org.opencontainers.image.title="GearUp Appointment Service" \
  org.opencontainers.image.description="Handles appointment management and scheduling within GearUp"

# Start the service
ENTRYPOINT ["java", "-jar", "app.jar"]
