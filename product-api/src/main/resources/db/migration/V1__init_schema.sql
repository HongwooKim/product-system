-- ═══════════════════════════════════════
-- Product System Schema
-- ═══════════════════════════════════════

-- 상품 마스터
CREATE TABLE products (
    id              VARCHAR(36) PRIMARY KEY,
    sku             VARCHAR(50) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    category_code   VARCHAR(100) NOT NULL,
    category_display_name VARCHAR(200) NOT NULL,
    parent_category_code VARCHAR(100),
    length_mm       INT NOT NULL,
    width_mm        INT NOT NULL,
    height_mm       INT NOT NULL,
    weight_g        INT NOT NULL,
    status          VARCHAR(30) NOT NULL,
    barcode         VARCHAR(50),
    image_url       VARCHAR(500),
    temperature_sensitive BOOLEAN DEFAULT FALSE,
    hazardous       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_category ON products(category_code);

-- 상품 풀필먼트 타입
CREATE TABLE product_fulfillment_types (
    product_id      VARCHAR(36) NOT NULL REFERENCES products(id),
    fulfillment_type VARCHAR(30) NOT NULL,
    PRIMARY KEY (product_id, fulfillment_type)
);

-- 셀러 오퍼
CREATE TABLE seller_offers (
    offer_id        VARCHAR(36) PRIMARY KEY,
    product_id      VARCHAR(36) NOT NULL REFERENCES products(id),
    seller_id       VARCHAR(36) NOT NULL,
    selling_price   DECIMAL(15,2) NOT NULL,
    selling_price_currency VARCHAR(3) NOT NULL DEFAULT 'KRW',
    supply_price    DECIMAL(15,2) NOT NULL,
    supply_price_currency VARCHAR(3) NOT NULL DEFAULT 'KRW',
    lead_time_days  INT NOT NULL DEFAULT 0,
    winner          BOOLEAN DEFAULT FALSE,
    active          BOOLEAN DEFAULT TRUE,
    last_updated    TIMESTAMP NOT NULL
);

CREATE INDEX idx_seller_offer_product ON seller_offers(product_id);
CREATE INDEX idx_seller_offer_seller ON seller_offers(seller_id);

-- FC 상품
CREATE TABLE fc_products (
    id                  VARCHAR(36) PRIMARY KEY,
    sku                 VARCHAR(50) NOT NULL,
    warehouse_id        VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    -- Storage Profile
    temperature_zone    VARCHAR(20) NOT NULL,
    storage_type        VARCHAR(20) NOT NULL,
    max_stack_height    INT NOT NULL DEFAULT 3,
    hazard_class        VARCHAR(20),
    -- Handling Rule
    fifo                BOOLEAN DEFAULT FALSE,
    expiry_managed      BOOLEAN DEFAULT FALSE,
    lot_tracking        BOOLEAN DEFAULT FALSE,
    fragile             BOOLEAN DEFAULT FALSE,
    -- Slotting
    primary_location    VARCHAR(30),
    replenish_location  VARCHAR(30),
    bulk_location       VARCHAR(30),
    pick_face_capacity  INT DEFAULT 0,
    -- Velocity
    velocity            VARCHAR(5) DEFAULT 'D',
    -- Replenishment Policy
    replenish_min_qty       INT DEFAULT 1,
    replenish_max_qty       INT DEFAULT 10,
    replenish_trigger_point INT DEFAULT 3,
    replenish_unit          VARCHAR(10) DEFAULT 'EACH',
    -- Dimensions (Product 복제본)
    length_mm           INT NOT NULL,
    width_mm            INT NOT NULL,
    height_mm           INT NOT NULL,
    weight_g            INT NOT NULL,
    -- Suspension
    suspension_reason   VARCHAR(30),
    -- Timestamps
    registered_at       TIMESTAMP NOT NULL,
    activated_at        TIMESTAMP,
    suspended_at        TIMESTAMP,
    UNIQUE (sku, warehouse_id)
);

CREATE INDEX idx_fcproduct_sku_warehouse ON fc_products(sku, warehouse_id);
CREATE INDEX idx_fcproduct_warehouse_status ON fc_products(warehouse_id, status);
CREATE INDEX idx_fcproduct_warehouse_velocity ON fc_products(warehouse_id, velocity);

-- FC 상품 풀필먼트 타입
CREATE TABLE fc_product_fulfillment_types (
    fc_product_id   VARCHAR(36) NOT NULL REFERENCES fc_products(id),
    fulfillment_type VARCHAR(30) NOT NULL,
    PRIMARY KEY (fc_product_id, fulfillment_type)
);
