/* Seed correlated sample data for chatbot-service
   Chat messages reference customer IDs so conversation history lines up.
*/
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname='public' AND tablename='chat_messages') THEN
    INSERT INTO chat_messages (id, customer_id, message, created_at)
    VALUES
      ('chat-1','cust-1','Hi, I need to change an appointment.', now()),
      ('chat-2','cust-2','How long will the repair take?', now())
    ON CONFLICT (id) DO NOTHING;
  END IF;
END$$;
