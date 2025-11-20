/* Seed correlated sample data for template-service
   Templates can be used by notification or other services.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='templates') THEN
    INSERT INTO templates (id, name, content, created_at)
    VALUES
      ('tmpl-1','appointment_reminder','Hello {{firstName}}, your appointment {{appointmentId}} is scheduled.', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
