--liquibase formatted sql

--changeset pricing:003-promotions-unique-active-index
CREATE UNIQUE INDEX idx_promotions_type_active
    ON promotions (type) WHERE active = true;
