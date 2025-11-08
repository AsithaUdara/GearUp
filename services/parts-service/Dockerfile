FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY target/lib /app/lib
COPY target/*.jar /app/app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8083
ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "com.gearup.PartsServiceApplication"]