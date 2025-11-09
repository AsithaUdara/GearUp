## Runtime Dockerfile — service-discovery (Eureka)
FROM eclipse-temurin:21-jre-alpine
# Install curl for healthcheck
RUN apk add --no-cache curl
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the prebuilt jar produced by root multi-module build
COPY service-discovery/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -f -u admin:password http://localhost:8761/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
