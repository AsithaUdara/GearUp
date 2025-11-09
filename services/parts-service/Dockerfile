# Simple Dockerfile for parts-service (uses pre-built JAR)
FROM eclipse-temurin:21-jre-alpine

# Create app user
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copy the pre-built JAR from host (assumes you ran: mvnw.cmd clean package)
COPY services/parts-service/target/parts-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8093

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8093/actuator/health || exit 1

LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Parts Service" \
    org.opencontainers.image.description="Parts Service for handling parts and materials requests"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

