## Multi-stage Dockerfile — user-auth-service

# Build stage: compile only the user-auth-service module (and required deps) from monorepo
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -B -DskipTests clean package -pl services/user-auth-service -am

# Runtime stage: minimal JRE image running the built jar
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/services/user-auth-service/target/user-auth-service-*.jar /app/app.jar
USER app

# Service listens on 8082 (aligned with application.yml)
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget -qO- --no-check-certificate http://localhost:8082/actuator/health || exit 1

LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"

ENTRYPOINT ["java","-jar","/app/app.jar"]
