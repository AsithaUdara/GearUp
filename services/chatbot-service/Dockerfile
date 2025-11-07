## Multi-stage Dockerfile — chatbot-service
## Builder stage: use Maven image to compile the service
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the entire repository (needed for parent POM and shared modules)
COPY . .

# Build only the chatbot-service module and its dependencies
RUN mvn -q -pl services/chatbot-service -am -DskipTests package

## Runtime stage: slim JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user (match naming convention used elsewhere)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built jar from builder stage
COPY --from=build /build/services/chatbot-service/target/*.jar app.jar

USER spring:spring
EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
