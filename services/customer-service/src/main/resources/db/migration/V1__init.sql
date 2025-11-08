CREATE TABLE IF NOT EXISTS customers (
  firebase_uid VARCHAR(64) PRIMARY KEY,
  email TEXT NOT NULL,
  display_name TEXT,
  phone TEXT,
  photo_url TEXT,
  id_number TEXT,
  address TEXT,
  birthday TEXT,
  kyc_status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS verification_documents (
  id UUID PRIMARY KEY,
  firebase_uid VARCHAR(64) NOT NULL REFERENCES customers(firebase_uid),
  doc_type TEXT NOT NULL,
  doc_url TEXT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

