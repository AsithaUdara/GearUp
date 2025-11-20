/* Seed correlated sample data for analytical-service
   Basic metrics referencing appointments/customers to validate cross-service analytics.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='metrics') THEN
    INSERT INTO metrics (id, metric_name, dimension, value, created_at)
    VALUES
      ('metric-1','appointments_count','all', 2, now()),
      ('metric-2','revenue_cents','all', 43000, now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
