## Runtime Dockerfile — user-auth-service
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the prebuilt service jar (built externally via multi-module Maven build)
COPY services/user-auth-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app

# Service listens on 8082 (aligned with application.yml)
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8082/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
