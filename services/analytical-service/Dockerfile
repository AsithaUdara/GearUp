# Stage 1: Lightweight runtime only
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy only the prebuilt JAR from your local system
# (Make sure you've already built it with mvnw clean package)
COPY services/analytical-service/target/analytical-service-*.jar app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8087

# Add health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

# Metadata
LABEL maintainer="ops@gearup.com" \
  org.opencontainers.image.version="1.0.0" \
  org.opencontainers.image.title="GearUp Analytical Service" \
  org.opencontainers.image.description="Analytical Service for GearUp - provides analytics, reports, and insights"

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
