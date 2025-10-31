## Multi-stage Dockerfile — service-discovery
FROM maven:3.10.1-jdk-21 AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
COPY src/ src/
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8761/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
FROM eclipse-temurin:21-jre-focal

# Build argument: jar file produced by Maven
ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} /app/service-discovery.jar

ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8761

# Healthcheck will use the actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/service-discovery.jar"]
