/* Seed correlated sample data for modification-service
   Modifications reference appointments and describe requested work.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='modifications') THEN
    INSERT INTO modifications (id, appointment_id, description, status, created_at)
    VALUES
      ('mod-1','appt-1','Install performance tires', 'REQUESTED', now()),
      ('mod-2','appt-2','Add roof rack', 'REQUESTED', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
