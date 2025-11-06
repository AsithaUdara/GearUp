FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy parent pom first (needed for dependency resolution)
COPY pom.xml ./

# Copy shared libraries
COPY shared-libs ./shared-libs

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Copy all module POMs (Maven reactor needs to see all modules)
COPY services/chatbot-service/pom.xml ./services/chatbot-service/
COPY services/automobile-service/pom.xml ./services/automobile-service/
COPY services/notification-service/pom.xml ./services/notification-service/
COPY services/template-service/pom.xml ./services/template-service/
COPY services/user-auth-service/pom.xml ./services/user-auth-service/
COPY service-discovery/pom.xml ./service-discovery/
COPY config-server/pom.xml ./config-server/
COPY api-gateway/pom.xml ./api-gateway/

# Copy chatbot service source only
COPY services/chatbot-service/src ./services/chatbot-service/src

# Build the application
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -pl services/chatbot-service -am

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from builder
COPY --from=builder /app/services/chatbot-service/target/*.jar app.jar

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
