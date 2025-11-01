# Local run & tests — api-gateway

This short snippet explains how to run the API Gateway and its tests locally on Windows PowerShell. It assumes you have Docker Desktop installed and running (Testcontainers requires a Docker engine).

## Run the gateway locally (development profile)

Open PowerShell and from the repository root run:

```powershell
# start the gateway with the 'dev' profile (keeps Eureka enabled if you run a local discovery server)
& '.\mvnw.cmd' -pl api-gateway -am spring-boot:run -Dspring-boot.run.profiles=dev

# or run the packaged jar from the module
cd api-gateway
& '..\mvnw.cmd' -DskipTests package
& 'java' -jar "target\api-gateway-1.0.0.jar" --spring.profiles.active=prod
```

Notes:

- If you rely on service discovery (Eureka) in `dev`, ensure your discovery server is running or disable discovery in `application.yml` for local runs.
- To enable Firebase in a local run, set one of the properties expected by `FirebaseConfig`:
  - `firebase.service-account-path` — path to the service account JSON file (recommended for local dev with a mounted file)
  - or `firebase.service-account-json` — inline JSON string (not recommended for long-lived configs)

In PowerShell you can set a session variable before running the app, for example:

```powershell
$env:FIREBASE_SERVICE_ACCOUNT_PATH = 'C:\secrets\firebase-service-account.json'
& '.\mvnw.cmd' -pl api-gateway -am spring-boot:run -Dspring-boot.run.profiles=dev
```

## Run the gateway tests (includes Testcontainers)

Tests use Testcontainers for the Redis-based rate-limiter integration. Docker Desktop must be running.

From repository root run (PowerShell):

```powershell
# run only the api-gateway module tests
& '.\mvnw.cmd' -pl api-gateway -am -DskipTests=false test

# or run with increased logging (helpful for debugging Testcontainers startup)
& '.\mvnw.cmd' -pl api-gateway -am -DskipTests=false -Dorg.slf4j.simpleLogger.defaultLogLevel=debug test
```

## Troubleshooting

- Docker: ensure Docker Desktop is running and has enough resources (at least 2GB memory) for Testcontainers.
- If Testcontainers fails to pull images due to network/proxy, configure Docker or your environment with the right proxy settings.
- If you want to skip Testcontainers-based integration tests and run only unit tests:

```powershell
& '.\mvnw.cmd' -pl api-gateway -am -DskipTests=false -Dtest="*Unit*" test
```

## Next steps & tips

- For CI, add the same `mvn` command to your pipeline and ensure the runner has Docker available.
- For production, prefer mounting the Firebase service account JSON (via secrets) and set `spring.profiles.active=prod` when starting the gateway.

If you want, I can add a short `Makefile`/PowerShell script to automate these local commands.
