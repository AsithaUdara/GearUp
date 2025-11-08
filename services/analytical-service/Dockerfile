## Multi-stage Dockerfile — analytical-service
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

COPY . /build
RUN mvn -B -DskipTests clean package -pl services/analytical-service -am

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/services/analytical-service/target/analytical-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="0.1.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
