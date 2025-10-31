FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/api-gateway-1.0.0.jar
COPY ${JAR_FILE} /app/api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/api-gateway.jar"]
