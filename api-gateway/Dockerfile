## Multi-stage Dockerfile — api-gateway
FROM maven:3.10.1-jdk-21 AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
COPY src/ src/
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -f http://localhost:8080/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/api-gateway-1.0.0.jar
COPY ${JAR_FILE} /app/api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/api-gateway.jar"]
