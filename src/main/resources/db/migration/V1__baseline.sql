-- ============================================================================
-- V1 baseline schema, reconstructed from the current JPA entity mappings.
--
-- IMPORTANT — read before relying on this against the existing Neon DB:
-- `spring.flyway.baseline-on-migrate=true` means Flyway will NOT execute
-- this script against a database that already has tables (i.e. the current
-- Neon production DB). Instead it will mark that DB as already being at
-- this baseline version. This script is what actually RUNS on a fresh
-- database (local Docker Postgres, CI, a new environment) — it needs to be
-- correct for those, but it will not touch Neon on first deploy.
--
-- Before switching Neon itself to ddl-auto=validate, run a schema dump
-- against it (e.g. `pg_dump --schema-only`) and diff it against this file,
-- especially for the `user_roles` table (see User.java for why) and the
-- `image.image` blob column type, since Hibernate's exact column type for
-- java.sql.Blob under the PostgreSQL dialect can vary (oid vs bytea)
-- depending on how the existing rows were written.
-- ============================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    inventory INTEGER NOT NULL DEFAULT 0,
    brand VARCHAR(255),
    category_id BIGINT REFERENCES category(id),
    seller_id BIGINT REFERENCES users(id)
);
CREATE INDEX idx_product_seller_id ON product(seller_id);
CREATE INDEX idx_product_category_id ON product(category_id);
CREATE INDEX idx_product_brand ON product(brand);

CREATE TABLE image (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    image OID,
    file_url VARCHAR(255),
    product_id BIGINT REFERENCES product(id) ON DELETE CASCADE
);
CREATE INDEX idx_image_product_id ON image(product_id);

CREATE TABLE cart (
    id BIGSERIAL PRIMARY KEY,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    user_id BIGINT UNIQUE REFERENCES users(id)
);

CREATE TABLE cart_item (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL DEFAULT 0,
    unit_price NUMERIC(19, 2),
    total_price NUMERIC(19, 2),
    product_id BIGINT REFERENCES product(id),
    cart_id BIGINT REFERENCES cart(id) ON DELETE CASCADE
);
CREATE INDEX idx_cart_item_cart_id ON cart_item(cart_id);

CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    order_date TIMESTAMP,
    cancel_date TIMESTAMP,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    order_status VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(id)
);
CREATE INDEX idx_orders_user_id ON customer_orders(user_id);

CREATE TABLE order_item (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL DEFAULT 0,
    price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    order_id BIGINT REFERENCES customer_orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES product(id)
);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);

CREATE TABLE token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT REFERENCES users(id)
);
CREATE INDEX idx_token_user_id ON token(user_id);
