## Multi-stage Dockerfile — service-discovery
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the prebuilt jar produced by the CI/root build into the image
COPY target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8761/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
