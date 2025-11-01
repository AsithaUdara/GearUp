# PostgreSQL provisioning — templates

This folder contains templates and examples for provisioning PostgreSQL for the microservices.

## Recommended production approach

- Use a managed Postgres service (RDS, Cloud SQL, Azure Database for PostgreSQL) when possible. It offloads backups, HA, and maintenance.
- If self-hosting on Kubernetes, use a statefulset + persistent volume(s) and a proper backup/restore plan.

## Per-service databases

The recommended naming policy:

- Database name: as\_<service> (e.g. as_user, as_trip)
- DB user: svc\_<service> (e.g. svc_user)
- Keep credentials in Kubernetes Secrets or the cloud provider's secret store.

## Files in this folder

- `secret-postgres.yml` - example Secret template (fill values and create per-service)
- `statefulset-postgres.yml` - a reusable StatefulSet template for self-hosted Postgres (replace names per service)
- `init-sql/` - optional bootstrap SQL to run when container first starts (local development)

## Notes

- These manifests are templates and must be adapted to your cluster (storage class, sizing, anti-affinity, resource limits).
- For production, prefer a managed DB and use the manifests here only for local clusters or non-critical environments.
