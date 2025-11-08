## Lightweight Dockerfile — payment-service
# Expects JAR to be built locally before Docker build
# Build context should be the repository root

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the pre-built JAR from local build
COPY services/payment-service/target/payment-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

LABEL maintainer="ops@gearup.com" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.title="GearUp Payment Service" \
      org.opencontainers.image.description="Payment Service for handling payment requests and customer bills"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
