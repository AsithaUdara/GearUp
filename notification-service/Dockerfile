FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . /workspace

# Build the application
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
