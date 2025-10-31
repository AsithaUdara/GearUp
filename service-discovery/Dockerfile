FROM eclipse-temurin:21-jre-focal

# Build argument: jar file produced by Maven
ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} /app/service-discovery.jar

ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8761

# Healthcheck will use the actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/service-discovery.jar"]
