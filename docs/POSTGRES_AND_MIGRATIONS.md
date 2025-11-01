PostgreSQL & Flyway migrations — Developer notes

This document explains how to run PostgreSQL locally for development and how to apply Flyway migrations for services in this monorepo.

Overview

- Local Postgres compose: `deployment/postgres/docker-compose.yml` (postgres:15).
- Helper script: `scripts/run-flyway-locally.ps1` — starts Postgres, creates DBs/users, and runs Flyway for services that have migrations.
- Migration files location per service: `services/<service>/src/main/resources/db/migration` (add SQL migration files here; Flyway expects filename order like V1**init.sql, V2**...)
- DB naming convention used in scripts/CI: database `as_<service>` and user `svc_<service>`.

Quick start (Windows / PowerShell)

1. Start Postgres (compose):

```powershell
docker compose -f deployment/postgres/docker-compose.yml up -d
```

2. Run migrations for all services (uses default password in compose unless you override):

```powershell
# from repo root
.\scripts\run-flyway-locally.ps1 -UseCompose -DbPassword 'changeme'
```

3. To migrate a single service:

```powershell
.\scripts\run-flyway-locally.ps1 -Service notification-service -DbPassword 'changeme'
```

Manual steps (psql + mvn flyway:migrate)

1. Create DB and user (example for notification-service):

```powershell
psql -h localhost -U postgres -c "CREATE USER svc_notification_service WITH PASSWORD 'pw';"
psql -h localhost -U postgres -c "CREATE DATABASE as_notification_service OWNER svc_notification_service;"
```

2. Run Flyway migrate from the service module (example):

```powershell
.\mvnw.cmd -pl services/notification-service flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/as_notification_service -Dflyway.user=svc_notification_service -Dflyway.password=pw
```

CI reference

- See `.github/workflows/ci-multi-migrate-test.yml` for a complete CI example: the workflow starts Postgres in GitHub Actions, discovers services with migrations, creates DBs/users, and runs `./mvnw flyway:migrate` per service.

Tips & gotchas

- Ensure `pg_isready` and `psql` are available when running scripts locally (the helper script uses `pg_isready` to detect readiness; GitHub Actions installs `postgresql-client`).
- Flyway migration files must be placed under `src/main/resources/db/migration` in each service for the helper & CI to detect them.
- Do not put production credentials in repo files; use `.env` and your secret manager for production.

If you want, I can add a short Postgres + Flyway quickstart to the root `README.md` or link this file from `DEV_GUIDE.md` and `deployment/docker/README.md`.

## Viewing the DB with pgAdmin

You can inspect the running Postgres databases using pgAdmin (desktop) or the `dpage/pgadmin4` Docker image. Below are quick instructions for both approaches.

1. pgAdmin (desktop)

- Install pgAdmin from https://www.pgadmin.org/ for your OS.
- Open pgAdmin and add a new server (right-click Servers -> Create -> Server...).
- Connection settings (example for local compose):

  - Name: GearUp Local
  - Host name/address: host.docker.internal # on Windows with Docker Desktop; use `localhost` if you mapped ports to the host
  - Port: 5432
  - Maintenance DB: postgres
  - Username: postgres (or `svc_<service>` if you created a service user)
  - Password: the value of `POSTGRES_PASSWORD` (e.g., `changeme` in the examples)

- After connecting, expand Servers -> Databases -> `as_<service>` to view schemas and run queries.

2. Run pgAdmin in Docker (quick temporary container)

```powershell
# run pgAdmin on port 5050 and expose the web UI
docker run --rm -d --name pgadmin4 -p 5050:80 \
	-e "PGADMIN_DEFAULT_EMAIL=dev@local" -e "PGADMIN_DEFAULT_PASSWORD=devpass" \
	dpage/pgadmin4
```

- Open your browser at `http://localhost:5050` and log in with the email/password above.
- Create a new server in pgAdmin with the same connection settings as the desktop instructions. For a Docker-to-Docker connection you may need to use the internal network alias (or `host.docker.internal` to reach the host Postgres from the pgAdmin container on Docker Desktop).

3. Notes & troubleshooting

- If your Postgres compose maps the container port to a host port other than `5432` (for example `5433`), use that host port in pgAdmin.
- On Windows + Docker Desktop, `host.docker.internal` reliably resolves to the host. On Linux you may need to use `localhost` or the Docker network alias.
- If you connect using a service-specific user (`svc_<service>`), use that username and its password when connecting; the DB name pattern used by scripts is `as_<service>`.

Example: to inspect the template-service DB created during testing (if you used `changeme` as the password):

- Host: `host.docker.internal`
- Port: `5433` (or `5432` if you used the default mapping)
- Database: `as_template_service`
- User: `svc_template_service`
- Password: `changeme`

Use the Query Tool in pgAdmin to run SELECTs against tables created by Flyway migrations, or use the Browser to inspect schemas and table data.
