@echo off
echo Building JARs locally...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed!
    exit /b 1
)

echo Building Docker images...
docker compose -f deployment/docker/docker-compose.yml build

echo Starting services...
docker compose -f deployment/docker/docker-compose.yml up -d

echo Done!
docker compose -f deployment/docker/docker-compose.yml ps
