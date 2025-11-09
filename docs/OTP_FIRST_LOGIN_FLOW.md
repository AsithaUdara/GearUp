# OTP-first Login and Role-based Redirect (Admin + Employee + Customer)

This document defines the end-to-end flow for first-time employee/admin login using an OTP, automatic Firebase UID creation, password setup, and role-based redirects. It also covers customer sign-up and login.

## Overview

- Admin creates a new user (employee or admin) and gets a one-time OTP.
- The user logs in the first time using their email + OTP to set a new password.
- During admin create, a Firebase user is created automatically and linked to the local user.
- After setting the password, login proceeds via Firebase Email/Password and the gateway redirects to the correct dashboard (ADMIN/EMPLOYEE/CUSTOMER).

## Backend services

- API Gateway: `http://localhost:8088`
- User Auth Service (internal via gateway): `lb://user-auth-service`
- Eureka (service discovery): `http://localhost:8761`
- Config Server (native): `http://localhost:8888`

## API Contracts

### 1) Admin: Create Employee/Admin

POST `/api/v1/admin/users/employees`

Body:
```
{
  "name": "John Employee",
  "email": "employee@gearup.com",
  "phoneNumber": "+94 7x xxx xxxx",
  "role": "EMPLOYEE" // or "ADMIN"
}
```

Response (201):
```
{
  "success": true,
  "message": "Employee created successfully. Share the OTP with the user.",
  "data": {
    "email": "employee@gearup.com",
    "otp": "123456",
    "expiresAt": "2025-11-08T10:15:30Z"
  }
}
```

Side effects:
- Local user is created with `isPasswordSet=false`.
- OTP is generated and stored.
- A Firebase user is created/ensured automatically and `firebaseUid` is stored on the local user (disabled until password setup).

### 2) First-time Password Setup (OTP)

POST `/api/v1/auth/setup-password`

Body:
```
{
  "email": "employee@gearup.com",
  "otp": "123456",
  "password": "NewStrongPassword!"
}
```

Response (200):
```
{
  "success": true,
  "message": "Password setup successful. You are now logged in.",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "email": "employee@gearup.com",
      "roles": [ { "name": "EMPLOYEE" } ],
      "isPasswordSet": true
    }
  }
}
```

Notes:
- OTP is validated and cleared.
- Firebase user is updated (password set, enabled, emailVerified=true) or created if missing.
- Local user is marked `isPasswordSet=true` and `firebaseUid` is ensured.

### 3) Normal Login (After password setup)

Auth is Firebase-first:
1. Client signs in with Firebase Email/Password to obtain an ID token.
2. Client calls backend: POST `/api/v1/auth/login` with `{ firebaseToken }`.
3. Backend issues its own JWTs and responds with user profile and role.

### 4) Customer Sign-up

- Frontend creates Firebase account with Email/Password.
- Then POST `/api/v1/users/register` body contains Firebase UID and profile:
```
{
  "firebaseUid": "<UID from Firebase>",
  "email": "customer@gearup.com",
  "displayName": "Jane Customer",
  "phoneNumber": "+94 7x xxx xxxx",
  "photoUrl": "...",
  "role": "CUSTOMER"
}
```
- Customer then logs in via Firebase -> `/api/v1/auth/login` and is redirected to `/customer/dashboard`.

## Frontend (Next.js) hooks & pages

- `src/app/components/login/LoginModal.tsx`: supports default login and OTP-first modal.
- `src/app/components/login/PasswordChangeModal.tsx`: calls `/api/v1/auth/setup-password` and on success calls `loginUser()` and redirects by role.
- `src/lib/authService.ts`: `loginUser` maps roles to dashboards:
  - ADMIN -> `/admin/dashboard`
  - EMPLOYEE -> `/employee/dashboard`
  - CUSTOMER -> `/customer/dashboard`
- `src/app/setup-password/page.tsx`: standalone page for OTP-based setup if needed.

## Role-based Redirects

- After password setup or normal login, the frontend extracts the primary role from the backend response and navigates to the appropriate dashboard.

## Ops & Dev Notes

- Docker compose: `deployment/docker/docker-compose.yml` orchestrates gateway, discovery, config, db, redis, rabbitmq, and user-auth-service.
- Firebase credentials:
  - For `user-auth-service`, credentials are resolved from classpath (`firebase-service-account.json`) via shared `security-lib`.
  - For `api-gateway`, dev disables Firebase init via `FIREBASE_SERVICE_ACCOUNT_JSON=` to avoid invalid JSON errors.
- The gateway health endpoint `/actuator/health` returns 401 in dev (secured).

## Admin Demo (your accounts)

- Admin: `admin@gearup.com` / `admin@gearup.com`
- Employee: `employee@gearup.com` / first-time via OTP, then set password
- Customer: `customer@gearup.com` / normal signup/login to `/customer/dashboard`

## Known Good Path

1) Login as admin in the frontend.
2) Create a new EMPLOYEE user; copy the OTP shown.
3) Logout admin.
4) Open the login modal -> choose OTP-first -> enter email + OTP -> set new password.
5) You’ll be redirected to `/employee/dashboard`.
6) Next logins use normal email/password (Firebase -> `/api/v1/auth/login`).
