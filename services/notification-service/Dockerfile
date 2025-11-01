## Multi-stage Dockerfile — notification-service
# Build context should be the repository root so local shared modules can be built
FROM openjdk:21-jdk AS builder
WORKDIR /workspace

# Copy full repository into build image
COPY . /workspace

# Build the notification-service module and any required local modules using the mvnw wrapper
RUN ./mvnw -B -DskipTests -pl services/notification-service -am package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the service jar produced by the reactor build
COPY --from=builder /workspace/services/notification-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -f http://localhost:8081/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
