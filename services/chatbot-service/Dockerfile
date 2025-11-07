# Use pre-built JAR from local build
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy pre-built JAR
COPY services/chatbot-service/target/*.jar app.jar

USER spring:spring

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
