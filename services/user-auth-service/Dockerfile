## Multi-stage Dockerfile — user-auth-service (self-contained build)

# Build stage: compile the module using Maven with JDK 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
# Copy the entire repository (build context must be repo root)
COPY . /build
# Build only this module and its dependencies
RUN mvn -B -DskipTests clean package -pl services/user-auth-service -am

# Runtime stage: run the built JAR on a slim JRE
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /build/services/user-auth-service/target/user-auth-service-*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
