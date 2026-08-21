-- SMALLINT (int2) does not match Integer (int4) expected by the Review entity.
-- Alter to INTEGER so Hibernate schema validation passes.
ALTER TABLE reviews
    ALTER COLUMN rating TYPE INTEGER;
