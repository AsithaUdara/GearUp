# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a secure non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy only the prebuilt JAR (ensure it's built before this step)
COPY services/tracking-service/target/tracking-service-*.jar /app/app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8091


# Health check to monitor service health
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8091/actuator/health || exit 1

# Metadata labels for observability
LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Tracking Service" \
    org.opencontainers.image.description="Service Tracking and Progress Management Service"

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
