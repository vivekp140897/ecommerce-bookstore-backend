-- CHAR(2) maps to bpchar in PostgreSQL, which Hibernate rejects as VARCHAR(2).
-- Alter the column to VARCHAR(2) to match the Address entity mapping.
ALTER TABLE addresses
    ALTER COLUMN country TYPE VARCHAR(2);
