/* Seed correlated sample data for notification-service
   Notifications reference customer IDs so they correlate with customer-service.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='notifications') THEN
    INSERT INTO notifications (id, customer_id, message, sent_at, status)
    VALUES
      ('notif-1','cust-1','Your appointment appt-1 is scheduled.', now(), 'SENT'),
      ('notif-2','cust-2','Your appointment appt-2 is scheduled.', now(), 'SENT')
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
