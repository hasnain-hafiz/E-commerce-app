-- V2: adds optimistic-locking support to product, and an index to back the
-- existing ILIKE-based ProductRepository.searchProducts query.
-- Same baseline-on-migrate caveat as V1: this only runs against a fresh
-- database. Applying it to the existing Neon DB requires it to actually
-- run (i.e. Neon's flyway_schema_history must already be past baseline) —
-- coordinate this migration's rollout with whoever manages the Neon deploy.

ALTER TABLE product ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Supports ProductRepository.searchProducts (name/brand/category ILIKE).
-- pg_trgm enables trigram indexes so ILIKE '%term%' can actually use an
-- index instead of a full sequential scan.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_product_name_trgm ON product USING gin (name gin_trgm_ops);
CREATE INDEX idx_product_brand_trgm ON product USING gin (brand gin_trgm_ops);
