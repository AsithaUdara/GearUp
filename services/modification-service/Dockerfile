# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a secure non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy the prebuilt service jar (CI will build the module before the Docker build)
# Use the exact jar name produced by the local Maven build.
COPY services/modification-service/target/modification-service.jar app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
# Align with compose which uses 8092 for this service
EXPOSE 8092

# Health check to monitor service health
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8092/actuator/health || exit 1

# Metadata labels for observability
LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Modification Service" \
    org.opencontainers.image.description="Handles modifications and updates within the GearUp platform"

# Start the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
