# Runtime stage: minimal JRE image running the built jar
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy the built artifact from the build stage
COPY services/template-service/target/template-service-*.jar /app/app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8085

# Health check to ensure the service is running
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Metadata labels for the Docker image
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0" \
    service="template-service" \
    description="Template service for the application platform"

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
