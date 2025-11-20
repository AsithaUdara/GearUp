/* Seed correlated sample data for customer-service
   This migration is idempotent and only inserts when the expected table exists.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='customers') THEN
    -- customers table uses `firebase_uid` as the identifier in this schema
    INSERT INTO customers (firebase_uid, display_name, email, created_at)
    SELECT 'cust-1', 'Alice Smith', 'alice@example.com', now()
    WHERE NOT EXISTS (SELECT 1 FROM customers WHERE firebase_uid = 'cust-1');

    INSERT INTO customers (firebase_uid, display_name, email, created_at)
    SELECT 'cust-2', 'Bob Jones', 'bob@example.com', now()
    WHERE NOT EXISTS (SELECT 1 FROM customers WHERE firebase_uid = 'cust-2');
  END IF;
END$$;
