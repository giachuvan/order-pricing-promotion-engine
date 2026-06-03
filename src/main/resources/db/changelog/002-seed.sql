--liquibase formatted sql

--changeset pricing:002-seed-promotions
INSERT INTO promotions (type, value, active) VALUES
    ('PERCENTAGE_DISCOUNT', 10, true),
    ('BUY2_GET1_FREE', 1, true),
    ('VIP_DISCOUNT', 5, true);

--changeset pricing:002-seed-coupons
INSERT INTO coupons (code, discount_amount, active, expiry_date) VALUES
    ('SUMMER10', 10, true, '2099-12-31'),
    ('SAVE20', 20, true, '2099-12-31');

--changeset pricing:002-seed-products
INSERT INTO products (sku, name, price) VALUES
    ('A100', 'Product A100', 100),
    ('B200', 'Product B200', 50);
