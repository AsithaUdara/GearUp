-- Ensure referential integrity behavior and helpful indexes/triggers

-- 1) Add an index on verification_documents.firebase_uid to speed up lookups
CREATE INDEX IF NOT EXISTS idx_verification_documents_firebase
    ON verification_documents(firebase_uid);

-- 2) Recreate FK from verification_documents -> customers with ON DELETE CASCADE
DO $$
BEGIN
    -- Drop the default-named FK if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_name = 'verification_documents'
          AND tc.constraint_name = 'verification_documents_firebase_uid_fkey'
    ) THEN
        ALTER TABLE verification_documents
            DROP CONSTRAINT verification_documents_firebase_uid_fkey;
    END IF;

    -- Drop our named FK if it exists (idempotency across re-runs)
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_name = 'verification_documents'
          AND tc.constraint_name = 'fk_verif_doc_customer'
    ) THEN
        ALTER TABLE verification_documents
            DROP CONSTRAINT fk_verif_doc_customer;
    END IF;

    -- Create the desired FK with ON DELETE CASCADE
    ALTER TABLE verification_documents
        ADD CONSTRAINT fk_verif_doc_customer
        FOREIGN KEY (firebase_uid)
        REFERENCES customers(firebase_uid)
        ON DELETE CASCADE;
END$$;

-- 3) Keep customers.updated_at in sync on row updates (server-side as a safety net)
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_customers_set_updated_at'
    ) THEN
        CREATE TRIGGER trg_customers_set_updated_at
            BEFORE UPDATE ON customers
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column();
    END IF;
END$$;

-- 4) Enforce valid KYC status values at the DB level
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'customers' AND constraint_type = 'CHECK'
          AND constraint_name = 'ck_customers_kyc_status_valid'
    ) THEN
        ALTER TABLE customers
          ADD CONSTRAINT ck_customers_kyc_status_valid
          CHECK (kyc_status IN ('NOT_STARTED','PENDING','VERIFIED','REJECTED'));
    END IF;
END$$;
