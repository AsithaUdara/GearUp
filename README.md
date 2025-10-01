# GearUp Automobile Service Backend

Secure Spring Boot backend with Firebase Authentication and environment-based secret management.

## Features
- Firebase Admin SDK initialization from `.env` (no secrets in repo)
- Stateless Spring Security with custom `FirebaseFilter`
- CORS configuration for local frontend
- Layered package structure (`config`, `security`, `controller`)

## Requirements
- Java 21
- Use Maven Wrapper (`mvnw.cmd`) if Maven not installed

## Setup
1. Create a `.env` file in project root.
2. Paste your Firebase service account JSON as a single line:
```
FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account","project_id":"your-project","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----\\n","client_email":"...","client_id":"...","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"..."}'
```
3. Ensure newline characters in the private key are escaped as `\\n`.

## Run
```powershell
./mvnw.cmd spring-boot:run
```
Or build & run jar:
```powershell
./mvnw.cmd clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Test Endpoints
Public: `GET http://localhost:8080/api/public/hello`
Secure: `GET http://localhost:8080/api/secure/hello` with header:
```
Authorization: Bearer <firebase-id-token>
```

## Troubleshooting
| Symptom | Cause | Fix |
|---------|-------|-----|
| 401 Unauthorized | Invalid / expired token | Refresh Firebase ID token |
| Firebase not initialized | Empty `.env` | Populate JSON correctly |
| mvn not recognized | Maven not installed | Use `mvnw.cmd` |

## Next Steps (Optional)
- Add SLF4J logging instead of `printStackTrace()`
- Add `spring-boot-starter-actuator`
- Externalize CORS origins via env var
- Write tests for `FirebaseFilter`

---
Professional, secure, and ready for review.
