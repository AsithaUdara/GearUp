# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S app && adduser -S app -G app

# Copy only the prebuilt JAR (ensure it's built before this step)
COPY services/chatbot-service/target/chatbot-service-*.jar app.jar

# Set proper ownership and switch to non-root user
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8086

# Health check to ensure service availability
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

# Metadata for better traceability
LABEL maintainer="ops@gearup.com" \
  org.opencontainers.image.version="1.0.0" \
  org.opencontainers.image.title="GearUp Chatbot Service" \
  org.opencontainers.image.description="AI-powered Chatbot Service for GearUp Platform"

# Start the service with optimal JVM flags for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
