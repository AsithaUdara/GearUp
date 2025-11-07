## Multi-stage Dockerfile — notification-service (self-contained build)

# Build stage: compile the module using Maven with JDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -B -DskipTests clean package -pl services/notification-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/services/notification-service/target/notification-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

LABEL maintainer="ops@company.com" \
      org.opencontainers.image.version="1.0.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
