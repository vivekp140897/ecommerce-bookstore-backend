-- CHAR(3) maps to bpchar in PostgreSQL, which Hibernate rejects as VARCHAR(3).
-- Alter the column to VARCHAR(3) to match the Payment entity mapping.
ALTER TABLE payments
    ALTER COLUMN currency TYPE VARCHAR(3);
