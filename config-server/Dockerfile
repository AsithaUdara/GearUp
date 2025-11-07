## Multi-stage Dockerfile — config-server
# Builder
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -q -pl config-server -am -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/config-server/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget --user=configadmin --password=change-me -qO- --no-check-certificate http://localhost:8888/actuator/health || exit 1
LABEL maintainer="ops@company.com" \
    org.opencontainers.image.version="0.1.0"
ENTRYPOINT ["java","-jar","/app/app.jar"]
