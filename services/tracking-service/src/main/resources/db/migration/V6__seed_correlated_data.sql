/* Seed correlated sample data for tracking-service
   Tracking entries reference appointment IDs to correlate flow.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='tracking_entries') THEN
    INSERT INTO tracking_entries (id, appointment_id, status, timestamp)
    VALUES
      ('track-1','appt-1','CHECKED_IN', now()),
      ('track-2','appt-2','SCHEDULED', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
