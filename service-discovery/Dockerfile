## Multi-stage Dockerfile — service-discovery
# Builder
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -q -pl service-discovery -am -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre-alpine
# Install wget for healthcheck
RUN apk add --no-cache wget
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/service-discovery/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8761/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
