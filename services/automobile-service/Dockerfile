FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . /workspace

# Build the application (skip tests for faster image builds by default)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the fat jar produced by Spring Boot
COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
