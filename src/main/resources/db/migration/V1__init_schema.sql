CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name    VARCHAR(100)  NOT NULL,
    last_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(50)   NOT NULL DEFAULT 'CUSTOMER',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL
);

-- Addresses
CREATE TABLE addresses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label       VARCHAR(100),
    line1       TEXT         NOT NULL,
    line2       TEXT,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(2)   NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Categories
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id   UUID REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Books
CREATE TABLE books (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255)   NOT NULL,
    author          VARCHAR(255)   NOT NULL,
    isbn            VARCHAR(20)    NOT NULL UNIQUE,
    description     TEXT,
    price           NUMERIC(10, 2) NOT NULL,
    stock_quantity  INTEGER        NOT NULL DEFAULT 0,
    cover_image_url VARCHAR(255),
    language        VARCHAR(10),
    page_count      INTEGER,
    published_date  DATE,
    publisher       VARCHAR(255),
    category_id     UUID           NOT NULL REFERENCES categories(id),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Carts
CREATE TABLE carts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Cart items
CREATE TABLE cart_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id    UUID           NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    book_id    UUID           NOT NULL REFERENCES books(id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT uq_cart_book UNIQUE (cart_id, book_id)
);

-- Orders
CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID           NOT NULL REFERENCES users(id),
    status              VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    shipping_address_id UUID           NOT NULL REFERENCES addresses(id),
    billing_address_id  UUID           REFERENCES addresses(id),
    coupon_code         VARCHAR(50),
    subtotal            NUMERIC(10, 2) NOT NULL,
    discount_amount     NUMERIC(10, 2) NOT NULL DEFAULT 0,
    shipping_cost       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    tax                 NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total               NUMERIC(10, 2) NOT NULL,
    placed_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Order items
CREATE TABLE order_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id    UUID           NOT NULL REFERENCES books(id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL
);

-- Payments
CREATE TABLE payments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       UUID           NOT NULL UNIQUE REFERENCES orders(id),
    status         VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    method         VARCHAR(50)    NOT NULL,
    amount         NUMERIC(10, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    transaction_id VARCHAR(255),
    processed_at   TIMESTAMPTZ
);

-- Reviews
CREATE TABLE reviews (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id           UUID        NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id           UUID        NOT NULL REFERENCES users(id),
    rating            INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title             VARCHAR(150),
    body              TEXT,
    verified_purchase BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_book_user_review UNIQUE (book_id, user_id)
);

-- Wishlists
CREATE TABLE wishlists (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

-- Wishlist items
CREATE TABLE wishlist_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wishlist_id UUID        NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    book_id     UUID        NOT NULL REFERENCES books(id),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wishlist_book UNIQUE (wishlist_id, book_id)
);

-- Indexes
CREATE INDEX idx_books_category ON books(category_id);
CREATE INDEX idx_books_price    ON books(price);
CREATE INDEX idx_reviews_book   ON reviews(book_id);
CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
