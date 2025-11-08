## Multi-stage Dockerfile — tracking-service
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

COPY . /build
RUN mvn -B -DskipTests clean package -pl services/tracking-service -am

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/services/tracking-service/target/tracking-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8091

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8091/actuator/health || exit 1

LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Tracking Service" \
    org.opencontainers.image.description="Service Tracking and Progress Management Service"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
