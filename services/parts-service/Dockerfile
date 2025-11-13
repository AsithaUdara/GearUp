# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a secure non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy the pre-built JAR from host (assumes you ran: mvnw.cmd clean package)
COPY services/parts-service/target/parts-service-*.jar /app/app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8093

# Health check to monitor service health
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8093/actuator/health || exit 1

# Metadata labels for observability
LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Parts Service" \
    org.opencontainers.image.description="Parts Service for handling parts and materials requests"

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

