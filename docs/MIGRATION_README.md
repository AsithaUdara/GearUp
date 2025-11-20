# MIGRATION_README — User Auth Data Migration

Purpose
- This Flyway migration imports master and sample data into the `as_user_auth_service` database: roles, permissions, role_permissions, users, user_roles, user_sessions, and user_audit_log.
- The migration is idempotent (uses `ON CONFLICT` where appropriate) and sets serial sequences to the highest existing id at the end.

Location
- `services/user-auth-service/src/main/resources/db/migration/V20251120__import_user_auth_data.sql`

Safety first (read before applying)
- Test on a staging environment that mirrors production before running in production.
- Back up the target database before applying the migration. Restoring from backup is the safest rollback.
- If you must avoid importing session tokens or personal emails, remove or sanitize sensitive rows in the SQL before applying.
- The migration preserves ids from the source; if those id ranges conflict with existing data, review and adjust accordingly.

What this migration does (high level)
1. Upserts `roles` by `name`.
2. Upserts `permissions` by `name`.
3. Inserts `role_permissions` (no-op if already present).
4. Upserts `users` by `email` (preserves `id`).
5. Inserts `user_roles` (no-op if already present).
6. Inserts or updates `user_sessions` (conflict target: `session_token`).
7. Inserts `user_audit_log` rows (no-op if already present).
8. Resets sequences using `setval(...)` so `nextval()` continues from the highest id.

How to apply
------------
Option A — Run with Flyway (recommended when Flyway is in use)

1. Commit this migration file in a PR and merge to the branch your CI/CD uses for deployments.
2. Flyway (CI/CD) will execute the migration during deployment if Flyway is configured.
3. To run locally against a staging DB with Maven:

```powershell
# From repository root (PowerShell)
cd "C:\Users\ASUS\Desktop\EAD auth flow\GearUp-backend"
.\mvnw.cmd -pl services/user-auth-service flyway:migrate -Dflyway.url=jdbc:postgresql://<HOST>:5432/<DB> -Dflyway.user=<USER> -Dflyway.password=<PASSWORD>
```

Replace `<HOST>`, `<DB>`, `<USER>`, and `<PASSWORD>` with your staging connection values. If Flyway is already configured via `application.yml` or environment variables you can omit the `-Dflyway.*` overrides.

Option B — Apply manually with `psql` (one-off)

```powershell
# Run from a machine with psql installed
psql -h <HOST> -p 5432 -U <USER> -d <DB> -f services/user-auth-service/src/main/resources/db/migration/V20251120__import_user_auth_data.sql

# Or from the Postgres Docker container (example container name: gearup-postgres)
docker cp services/user-auth-service/src/main/resources/db/migration/V20251120__import_user_auth_data.sql gearup-postgres:/tmp/migration.sql
docker exec -i gearup-postgres psql -U postgres -d as_user_auth_service -f /tmp/migration.sql
```

Verification (queries to run after the migration)
------------------------------------------------
-- Basic counts
SELECT 'users' AS table, COUNT(*) FROM users;
SELECT 'roles' AS table, COUNT(*) FROM roles;
SELECT 'permissions' AS table, COUNT(*) FROM permissions;

-- Verify admin user exists
SELECT id, email, first_name, last_name, created_at FROM users WHERE email = 'admin@gearup.com';

-- Check roles assigned to a user (replace <USER_ID>)
SELECT ur.user_id, r.name FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = <USER_ID>;

-- Confirm sequences advanced to max(id)
SELECT pg_get_serial_sequence('public.users','id') AS users_seq, last_value FROM pg_sequences WHERE schemaname='public' AND sequencename=substring(pg_get_serial_sequence('public.users','id') from '[^\.]+$');

Common issues & fixes
---------------------
- Duplicate key violations: If you see constraint errors, inspect the target constraint and confirm the migration's conflict target matches (e.g., `ON CONFLICT (email)` or a named unique constraint).
- Permission denied: Ensure the Flyway/DB user has INSERT/UPDATE privileges.
- Checksum mismatch in Flyway: Do not edit migrations that have already been applied to production. Instead, add a new corrective migration.

Rollback guidance
-----------------
- Prefer restoring a DB backup taken before applying the migration.
- Manual deletion of inserted rows is risky — only do this when you can reliably identify migrated rows (use `created_at` timestamps or source ids), and coordinate with dependent services.

Security & privacy
------------------
- This migration contains emails and session tokens from a development export. Sanitize or remove sensitive values before applying to environments where such data is not permitted.
- Never store production credentials in plaintext or in repository. Use environment variables or your CI secret store.

PR checklist
------------
- [ ] Confirm at least one code review approval.
- [ ] Confirm a backup of the target database exists and has been tested for restore.
- [ ] Verify migration in staging and run the verification queries above.
- [ ] Confirm whether session tokens and PII are acceptable in the target environment.
- [ ] Add a short note to project `docs/` or release notes describing why this migration is required.

Optional follow-ups I can do for you
-----------------------------------
- Produce an anonymized variant that replaces emails and tokens with placeholders.
- Split the migration into smaller per-table Flyway migrations for easier review.
- Add an environment guard so the migration only runs when `MIGRATE_USER_DATA=true` (requires code change to conditionalize Flyway or an alternate process).

Contact / last updated
-----------------------
If you want changes, tell me which option above and I'll update the migration or README accordingly.

Last updated: 2025-11-20

-- Count rows
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM permissions;

-- Check a known user exists
SELECT id, email, first_name, last_name, created_at FROM users WHERE email = 'admin@gearup.com';

-- Check user roles (replace <ADMIN_USER_ID>)
SELECT ur.user_id, r.name FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = <ADMIN_USER_ID>;

-- Confirm sequences
SELECT pg_get_serial_sequence('public.users','id') AS users_seq, nextval(pg_get_serial_sequence('public.users','id'));
SELECT pg_get_serial_sequence('public.roles','id') AS roles_seq, nextval(pg_get_serial_sequence('public.roles','id'));

Rollback guidance
------------------

- The safest rollback is to restore from a pre-migration database backup.
- If a full restore is not possible, consider carefully removing rows inserted by this migration using filters on `created_at` and `id` ranges — this is risky and requires review.
- Deleting rows that other services reference can break production; prefer backups and restore.

Security and privacy
--------------------

- This migration contains real emails and session tokens copied from a development environment. Do NOT apply to production if those values are sensitive. If required, sanitize `email`, `session_token`, and any PII before applying.
- Do not commit production credentials into the repository. Use environment variables, CI secret stores, or the project's secret manager for DB credentials.

PR Checklist for maintainers
---------------------------

- [ ] Confirm review and approval from at least one team lead.
- [ ] Confirm a backup of the target DB was taken and recovery tested.
- [ ] Confirm the migration has been tested on staging and verified using the queries above.
- [ ] Confirm any sensitive values in the migration are anonymized or that the team accepts the data as-is.
- [ ] Add a short entry to `docs/` or release notes describing why this migration was added.

Troubleshooting
---------------

- Error: duplicate key value violates unique constraint — the migration should upsert, but a constraint that differs from expectations may exist; inspect the constraint and consider adjusting the conflict target in the migration.
- Error: permission denied — ensure the Flyway/psql user has INSERT/UPDATE privileges on target tables.
- If Flyway reports checksum mismatch for an already-applied migration, do NOT change files already applied to production; instead add a new migration that performs corrective actions.

If you want me to:

- Split this migration into smaller per-table migration files, or
- Produce an anonymized variant that replaces emails and tokens with safe placeholders, or
- Add a safety guard so the migration only runs when `MIGRATE_USER_DATA=true`,

tell me which option and I will prepare the change.

---
Last updated: 2025-11-20
