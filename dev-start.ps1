#!/usr/bin/env pwsh
# Quick development setup script

# Set environment variables for Maven
$env:MAVEN_OPTS = "-Xmx2g -XX:+UseG1GC"

# Start development database
Write-Host "Starting development database..."
docker-compose -f deployment/docker/docker-compose.dev.yml --env-file deployment/docker/dev.env up -d

# Wait for database to be ready
Write-Host "Waiting for database to be ready..."
Start-Sleep -Seconds 10

# Run parts-service in development mode
Write-Host "Starting parts-service..."
./mvnw.cmd -T1C -pl services/parts-service spring-boot:run -Dspring-boot.run.profiles=dev