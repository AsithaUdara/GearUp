## Multi-stage Dockerfile — user-auth-service

# Build stage: compile the module using Maven with JDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -B -DskipTests clean package -pl services/user-auth-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
# Lightweight runtime-only image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a secure non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

COPY --from=build /build/services/user-auth-service/target/user-auth-service-*.jar /app/app.jar
# Copy the prebuilt service jar (CI will build the module before the Docker build)
COPY services/user-auth-service/target/user-auth-service-*.jar /app/app.jar

# Set ownership and permissions
RUN chown -R app:app /app
USER app

# Expose service port
EXPOSE 8082

# Health check to monitor service health
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Metadata labels for observability
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0" \
    org.opencontainers.image.title="User Authentication Service" \
    org.opencontainers.image.description="Handles user authentication and authorization"

# Start the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
