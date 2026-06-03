--liquibase formatted sql

--changeset pricing:001-schema
CREATE TABLE products (
    sku         VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,
    CONSTRAINT chk_products_price_positive CHECK (price > 0)
);

CREATE TABLE promotions (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(50) NOT NULL,
    value       NUMERIC(12, 2) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_promotions_value_positive CHECK (value > 0)
);

CREATE TABLE coupons (
    code              VARCHAR(50) PRIMARY KEY,
    discount_amount   NUMERIC(12, 2) NOT NULL,
    active            BOOLEAN NOT NULL DEFAULT true,
    expiry_date       DATE NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version           BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_coupons_discount_positive CHECK (discount_amount > 0)
);

CREATE TABLE orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type   VARCHAR(20) NOT NULL,
    coupon_code     VARCHAR(50),
    subtotal        NUMERIC(12, 2) NOT NULL,
    total_discount  NUMERIC(12, 2) NOT NULL,
    final_price     NUMERIC(12, 2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_subtotal_non_negative CHECK (subtotal >= 0),
    CONSTRAINT chk_orders_total_discount_non_negative CHECK (total_discount >= 0),
    CONSTRAINT chk_orders_final_price_non_negative CHECK (final_price >= 0)
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    sku         VARCHAR(50) NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,
    quantity    INTEGER NOT NULL,
    line_total  NUMERIC(12, 2) NOT NULL,
    CONSTRAINT chk_order_items_price_positive CHECK (price > 0),
    CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
