# Multi-stage build for modification-service

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -B -DskipTests clean package -pl services/modification-service -am

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
# Spring Boot fat jar has fixed name 'modification-service.jar' (finalName in pom.xml)
COPY --from=build /build/services/modification-service/target/modification-service.jar /app/app.jar
RUN chown -R app:app /app
USER app
# Document the correct internal port (matches docker-compose and config)
EXPOSE 8092

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
	CMD wget --no-verbose --tries=1 --spider http://localhost:8092/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
