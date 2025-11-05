# Backups & restore — PostgreSQL

This document outlines recommended backup and restore practices for PostgreSQL used by the microservices.

## Production: use managed backups

- If you use a managed DB (RDS/Cloud SQL/Azure DB): enable automated daily backups, point-in-time recovery (PITR) if available, and configure retention and snapshots according to your RPO/RTO.
- Test restores regularly to a sandbox environment.

## Self-hosted on Kubernetes

- Use scheduled pg_dump or physical base backups (pg_basebackup) combined with WAL archiving for PITR.
- Popular tools: pgBackRest, WAL-G, or Velero combined with persistent volume snapshots (with caution).

Simple logical backup (recommended for smaller DBs / ad-hoc restores)

Create backup (dump):

```bash
PGPASSWORD="$DB_PASSWORD" pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME -F c -b -v -f "/backups/${DB_NAME}-$(date +%F).dump"
```

Restore from dump:

```bash
PGPASSWORD="$DB_PASSWORD" pg_restore -h $DB_HOST -U $DB_USER -d $DB_NAME -v "/backups/${DB_NAME}-2025-01-01.dump"
```

## Disaster recovery runbook (summary)

1. Identify the last good backup/snapshot and the WAL segments (if PITR).
2. Create a replacement DB instance or PVC + pod and restore the base backup.
3. Apply WAL segments to reach desired point-in-time (if using PITR).
4. Run smoke tests and verify data integrity.
5. Promote the restored instance and update service connection strings (or restore to a sandbox and migrate data).

## Testing backups

- Regularly run restore tests into an isolated environment.
- Automate periodic verification (run an application smoke test against the restored DB).

## Documentation & playbooks

- Keep a runnable playbook in `docs/` (this file). Include steps to rotate credentials and revoke access when performing maintenance.
