-- Fix all remaining schema/entity type mismatches in one migration.
--
-- 1. users.role: VARCHAR(20) → VARCHAR(50)
--    @Enumerated(STRING) with @Column(length=50) expects varchar(50).
ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(50);

-- 2. books.cover_image_url: TEXT → VARCHAR(255)
--    Bare String field (no @Column) expects varchar(255).
ALTER TABLE books
    ALTER COLUMN cover_image_url TYPE VARCHAR(255);

-- 3. orders.status: VARCHAR(20) → VARCHAR(50)
--    @Enumerated(STRING) with @Column(length=50) expects varchar(50).
ALTER TABLE orders
    ALTER COLUMN status TYPE VARCHAR(50);

-- 4. payments.status: VARCHAR(20) → VARCHAR(50)
--    @Enumerated(STRING) with @Column(length=50) expects varchar(50).
ALTER TABLE payments
    ALTER COLUMN status TYPE VARCHAR(50);

-- 5. payments.method: VARCHAR(20) → VARCHAR(50)
--    @Enumerated(STRING) with @Column(length=50) expects varchar(50).
ALTER TABLE payments
    ALTER COLUMN method TYPE VARCHAR(50);
