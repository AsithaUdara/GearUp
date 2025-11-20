/* Seed correlated sample data for payment-service
   Payments reference appointments created in appointment-service.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='payments') THEN
    INSERT INTO payments (id, appointment_id, amount_cents, currency, status, created_at)
    VALUES
      ('pay-1','appt-1', 25000, 'USD', 'COMPLETED', now()),
      ('pay-2','appt-2', 18000, 'USD', 'PENDING', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
