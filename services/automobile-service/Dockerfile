## Multi-stage Dockerfile — automobile-service
# This Dockerfile expects the build context to be the repository root so it can
# build the module and its local shared dependencies using a reactor build.

FROM openjdk:21-jdk AS builder
WORKDIR /workspace

# Copy the whole repository into the build image (build context should be repo root)
COPY . /workspace

# Use the project's Maven wrapper so we don't depend on an external maven image tag.
# The wrapper will download Maven inside the build image.
RUN ./mvnw -B -DskipTests -pl services/automobile-service -am package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the service jar produced by the reactor build
COPY --from=builder /workspace/services/automobile-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8080/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
