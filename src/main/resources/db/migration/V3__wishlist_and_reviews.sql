-- V3: Wishlist + Reviews (Phase 2b).
-- Same baseline-on-migrate caveat as V1/V2 — only runs on a fresh schema
-- unless the existing Neon DB has already been advanced past V2.

CREATE TABLE wishlist_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
);
CREATE INDEX idx_wishlist_user_id ON wishlist_item(user_id);

CREATE TABLE product_review (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_review_user_product UNIQUE (user_id, product_id)
);
CREATE INDEX idx_review_product_id ON product_review(product_id);
