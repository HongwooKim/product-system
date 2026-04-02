-- ═══════════════════════════════════════
-- CQRS Read Model Tables
-- Write Model(products, fc_products)과 독립.
-- Projector가 도메인 이벤트를 소비하여 비동기 갱신.
-- ═══════════════════════════════════════

-- 주문 시스템용 뷰
CREATE TABLE read_order_product_view (
    sku                     VARCHAR(50) PRIMARY KEY,
    product_name            VARCHAR(200),
    category_code           VARCHAR(100),
    product_status          VARCHAR(30),
    winner_seller_id        VARCHAR(36),
    winner_price            DECIMAL(15,2),
    winner_price_currency   VARCHAR(3),
    active_fc_count         INT DEFAULT 0,
    active_fc_list          VARCHAR(500),
    has_cold_chain_fc       BOOLEAN DEFAULT FALSE,
    length_mm               INT,
    width_mm                INT,
    height_mm               INT,
    weight_g                INT,
    temperature_sensitive   BOOLEAN DEFAULT FALSE,
    oversized               BOOLEAN DEFAULT FALSE,
    purchasable             BOOLEAN DEFAULT FALSE,
    last_updated            TIMESTAMP
);

CREATE INDEX idx_read_order_sku ON read_order_product_view(sku);
CREATE INDEX idx_read_order_purchasable ON read_order_product_view(purchasable);

-- 피킹 시스템용 뷰
CREATE TABLE read_picking_product_view (
    fc_product_id           VARCHAR(36) PRIMARY KEY,
    sku                     VARCHAR(50),
    warehouse_id            VARCHAR(50),
    status                  VARCHAR(20),
    operational             BOOLEAN DEFAULT FALSE,
    primary_location        VARCHAR(30),
    replenish_location      VARCHAR(30),
    bulk_location           VARCHAR(30),
    pick_face_capacity      INT,
    velocity                VARCHAR(5),
    fifo                    BOOLEAN DEFAULT FALSE,
    expiry_managed          BOOLEAN DEFAULT FALSE,
    fragile                 BOOLEAN DEFAULT FALSE,
    replenish_trigger_point INT,
    replenish_max_qty       INT,
    replenish_unit          VARCHAR(10),
    temperature_zone        VARCHAR(20),
    weight_g                INT,
    oversized               BOOLEAN DEFAULT FALSE,
    last_updated            TIMESTAMP
);

CREATE INDEX idx_read_picking_sku_wh ON read_picking_product_view(sku, warehouse_id);
CREATE INDEX idx_read_picking_wh_velocity ON read_picking_product_view(warehouse_id, velocity);
CREATE INDEX idx_read_picking_location ON read_picking_product_view(primary_location);
CREATE INDEX idx_read_picking_wh_operational ON read_picking_product_view(warehouse_id, operational);

-- 대시보드/운영팀용 뷰
CREATE TABLE read_dashboard_product_view (
    fc_product_id           VARCHAR(36) PRIMARY KEY,
    sku                     VARCHAR(50),
    product_name            VARCHAR(200),
    warehouse_id            VARCHAR(50),
    status                  VARCHAR(20),
    category_code           VARCHAR(100),
    temperature_zone        VARCHAR(20),
    storage_type            VARCHAR(20),
    primary_location        VARCHAR(30),
    velocity                VARCHAR(5),
    replenish_trigger_point INT,
    replenish_max_qty       INT,
    slotting_zone_type      VARCHAR(30),
    slotting_reason         VARCHAR(500),
    fulfillment_types       VARCHAR(200),
    registered_at           TIMESTAMP,
    activated_at            TIMESTAMP,
    last_updated            TIMESTAMP
);

CREATE INDEX idx_read_dash_wh_status ON read_dashboard_product_view(warehouse_id, status);
CREATE INDEX idx_read_dash_wh_velocity ON read_dashboard_product_view(warehouse_id, velocity);
CREATE INDEX idx_read_dash_wh_temp ON read_dashboard_product_view(warehouse_id, temperature_zone);
