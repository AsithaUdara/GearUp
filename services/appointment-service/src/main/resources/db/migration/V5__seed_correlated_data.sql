/* Seed correlated sample data for appointment-service
   Appointments reference customer and vehicle IDs used in other services.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='appointments') THEN
    INSERT INTO appointments (id, customer_id, vehicle_id, scheduled_at, status, created_at)
    VALUES
      ('appt-1','cust-1','veh-1', now() + interval '2 days', 'SCHEDULED', now()),
      ('appt-2','cust-2','veh-2', now() + interval '3 days', 'SCHEDULED', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
