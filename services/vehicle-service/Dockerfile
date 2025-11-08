## Multi-stage Dockerfile — vehicle-service

# Build stage: compile the module using Maven with JDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
# Copy the entire repository (build context must be repo root)
COPY . /build
# Build only this module and its dependencies
RUN mvn -B -DskipTests clean package -pl services/vehicle-service -am

# Runtime stage: run the built JAR on a slim JRE
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /build/services/vehicle-service/target/vehicle-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8090
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1
LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    service="vehicle-service" \
    description="Vehicle management and tracking service for GearUp platform"
ENTRYPOINT ["java","-jar","/app/app.jar"]
