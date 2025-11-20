/* Seed correlated sample data for parts-service
   Part requests are linked to appointments to show correlated data.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='parts_requests') THEN
    -- schema uses request_id/material/quantity/status; id is UUID with default
    INSERT INTO parts_requests (request_id, material, quantity, status, created_by, created_at, updated_at)
    SELECT 'part-1', 'Brake Pad', 4, 'PENDING', gen_random_uuid(), now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM parts_requests WHERE request_id = 'part-1');

    INSERT INTO parts_requests (request_id, material, quantity, status, created_by, created_at, updated_at)
    SELECT 'part-2', 'Air Filter', 1, 'PENDING', gen_random_uuid(), now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM parts_requests WHERE request_id = 'part-2');
  END IF;
END$$;
