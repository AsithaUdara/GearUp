## Multi-stage Dockerfile — api-gateway
# Builder
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -q -pl api-gateway -am -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre AS runtime
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -f http://localhost:8080/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
