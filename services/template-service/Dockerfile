## Multi-stage Dockerfile — template-service

# Build stage: compile only the template-service module (and any dependent modules) from the monorepo
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -B -DskipTests clean package -pl services/template-service -am

# Runtime stage: minimal JRE image running the built jar
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/services/template-service/target/template-service-*.jar /app/app.jar
USER app
EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
