/* Seed correlated sample data for vehicle-service
   Links vehicles to customers by using the same customer IDs as customer-service.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='vehicles') THEN
    -- Adjusted to match current vehicles table schema (uses user_id and number_plate)
    INSERT INTO vehicles (id, user_id, make, model, year, number_plate, created_at)
    SELECT gen_random_uuid(), 'cust-1', 'Honda', 'Accord', 2018, 'ABC-123', now()
    WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE user_id = 'cust-1' AND make='Honda' AND model='Accord');

    INSERT INTO vehicles (id, user_id, make, model, year, number_plate, created_at)
    SELECT gen_random_uuid(), 'cust-2', 'Toyota', 'Camry', 2020, 'XYZ-789', now()
    WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE user_id = 'cust-2' AND make='Toyota' AND model='Camry');
  END IF;
END$$;
