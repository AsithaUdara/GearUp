# Local init SQL

This folder can hold initialization SQL scripts used for local or test environments.

## How to use

- For local Docker Compose setups you can mount the folder into the Postgres container at `/docker-entrypoint-initdb.d` so the scripts run on first startup.
- In Kubernetes, you can use an init container or an operator to apply bootstrap SQL when the database is first created.

Example file names:

- `00-create-extension.sql` — create any required extensions (uuid-ossp, pgcrypto)
- `01-create-schema.sql` — create roles and per-service schemas

Be careful: these scripts run only at first initialization of the database cluster. For schema evolution use Flyway migrations inside each service.
