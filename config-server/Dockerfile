FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY target/lib /app/lib
COPY target/*.jar /app/app.jar

RUN chown -R app:app /app
USER app
EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- --no-check-certificate http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "com.gearup.configserver.ConfigServerApplication"]
