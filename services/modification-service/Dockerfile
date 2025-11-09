## Multi-stage Dockerfile — modification-service
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the prebuilt service jar (CI will build the module before the Docker build)
COPY services/modification-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8089
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8089/actuator/health || exit 1
LABEL maintainer="ops@gearup.com" \
    org.opencontainers.image.version="1.0.0" \
    org.opencontainers.image.title="GearUp Modification Service"
ENTRYPOINT ["java","-jar","/app/app.jar"]