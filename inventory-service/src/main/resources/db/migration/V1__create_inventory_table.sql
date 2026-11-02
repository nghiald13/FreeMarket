-- ---------------------------------------------------------------------
-- 0. Helpers
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE SEQUENCE order_number_seq START WITH 1000000 INCREMENT BY 1;

-- ---------------------------------------------------------------------
-- 1. CATALOG
-- ---------------------------------------------------------------------
CREATE TABLE category
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    parent_id   UUID REFERENCES category (id) ON DELETE RESTRICT,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_category_slug UNIQUE (slug),
    CONSTRAINT ck_category_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id)
);
CREATE INDEX idx_category_parent ON category (parent_id);

CREATE TABLE brand
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL,
    logo_url   VARCHAR(1024),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_brand_name UNIQUE (name),
    CONSTRAINT uq_brand_slug UNIQUE (slug)
);

CREATE TABLE product
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    merchant_id   UUID         NOT NULL,   -- người bán (merchant/user-service), không FK
    category_id   UUID REFERENCES category (id) ON DELETE SET NULL,
    brand_id      UUID REFERENCES brand (id) ON DELETE SET NULL,
    name          VARCHAR(500) NOT NULL,
    slug          VARCHAR(500) NOT NULL,
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    -- Thuộc tính tự do theo từng loại sản phẩm (thay cho việc cần MongoDB)
    attributes    JSONB        NOT NULL DEFAULT '{}'::jsonb,
    -- Full-text search (config 'simple' vì không có dictionary tiếng Việt built-in)
    search_vector TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(name, '') || ' ' || coalesce(description, ''))
        ) STORED,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_product_slug UNIQUE (slug),
    CONSTRAINT ck_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'))
);
CREATE INDEX idx_product_merchant ON product (merchant_id, status);
CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_brand ON product (brand_id);
CREATE INDEX idx_product_status ON product (status);
CREATE INDEX idx_product_attributes ON product USING GIN (attributes jsonb_path_ops);
CREATE INDEX idx_product_search ON product USING GIN (search_vector);

-- Biến thể bán được (SKU). Sản phẩm không có biến thể vẫn có 1 variant mặc định.
CREATE TABLE product_variant
(
    id             UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    product_id     UUID          NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    sku            VARCHAR(100)  NOT NULL,
    barcode        VARCHAR(100),
    name           VARCHAR(500),
    price          NUMERIC(19, 2) NOT NULL,
    compare_at_price NUMERIC(19, 2),
    cost_price     NUMERIC(19, 2),
    currency       CHAR(3)       NOT NULL DEFAULT 'VND',
    weight_grams   INT,
    -- Tuỳ chọn của biến thể, ví dụ {"color":"red","size":"M"}
    options        JSONB         NOT NULL DEFAULT '{}'::jsonb,
    is_default     BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version        BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_variant_sku UNIQUE (sku),
    CONSTRAINT ck_variant_price CHECK (price >= 0),
    CONSTRAINT ck_variant_compare_price CHECK (compare_at_price IS NULL OR compare_at_price >= 0),
    CONSTRAINT ck_variant_cost CHECK (cost_price IS NULL OR cost_price >= 0)
);
CREATE INDEX idx_variant_product ON product_variant (product_id);
CREATE UNIQUE INDEX uq_variant_one_default_per_product
    ON product_variant (product_id) WHERE is_default;

CREATE TABLE product_image
(
    id         UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    product_id UUID          NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variant (id) ON DELETE SET NULL,
    url        VARCHAR(1024) NOT NULL,
    alt_text   VARCHAR(255),
    sort_order INT           NOT NULL DEFAULT 0,
    is_primary BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_product_image_product ON product_image (product_id);
CREATE UNIQUE INDEX uq_product_one_primary_image
    ON product_image (product_id) WHERE is_primary;

-- ---------------------------------------------------------------------
-- 2. INVENTORY (tồn kho)
-- ---------------------------------------------------------------------
CREATE TABLE warehouse
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    address    JSONB,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_warehouse_code UNIQUE (code)
);

CREATE TABLE stock_item
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    variant_id         UUID        NOT NULL REFERENCES product_variant (id) ON DELETE CASCADE,
    warehouse_id       UUID        NOT NULL REFERENCES warehouse (id) ON DELETE RESTRICT,
    quantity_on_hand   INT         NOT NULL DEFAULT 0,
    quantity_reserved  INT         NOT NULL DEFAULT 0,
    quantity_available INT GENERATED ALWAYS AS (quantity_on_hand - quantity_reserved) STORED,
    reorder_level      INT         NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    version            BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_stock_variant_warehouse UNIQUE (variant_id, warehouse_id),
    CONSTRAINT ck_stock_on_hand CHECK (quantity_on_hand >= 0),
    CONSTRAINT ck_stock_reserved CHECK (quantity_reserved >= 0 AND quantity_reserved <= quantity_on_hand)
);
CREATE INDEX idx_stock_warehouse ON stock_item (warehouse_id);

-- Sổ cái biến động kho (append-only) để audit / đối soát
CREATE TABLE stock_movement
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    stock_item_id   UUID        NOT NULL REFERENCES stock_item (id) ON DELETE CASCADE,
    movement_type   VARCHAR(30) NOT NULL,
    quantity_delta  INT         NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    UUID,
    note            VARCHAR(500),
    created_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_movement_type CHECK (movement_type IN
        ('INBOUND', 'OUTBOUND', 'RESERVE', 'RELEASE', 'ADJUSTMENT', 'RETURN', 'TRANSFER')),
    CONSTRAINT ck_movement_delta CHECK (quantity_delta <> 0)
);
CREATE INDEX idx_stock_movement_item ON stock_movement (stock_item_id, created_at DESC);
CREATE INDEX idx_stock_movement_ref ON stock_movement (reference_type, reference_id);

-- Giữ hàng cho đơn (có TTL để tự nhả nếu thanh toán không hoàn tất)
CREATE TABLE stock_reservation
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    stock_item_id UUID        NOT NULL REFERENCES stock_item (id) ON DELETE CASCADE,
    order_id      UUID        NOT NULL,   -- FK thêm sau khi tạo bảng orders
    quantity      INT         NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at    TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    version       BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_reservation_order_item UNIQUE (order_id, stock_item_id),
    CONSTRAINT ck_reservation_qty CHECK (quantity > 0),
    CONSTRAINT ck_reservation_status CHECK (status IN ('ACTIVE', 'COMMITTED', 'RELEASED', 'EXPIRED'))
);
CREATE INDEX idx_reservation_expiry ON stock_reservation (expires_at) WHERE status = 'ACTIVE';

-- ---------------------------------------------------------------------
-- 3. ORDER
-- ---------------------------------------------------------------------
CREATE TABLE orders
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_number     BIGINT         NOT NULL DEFAULT nextval('order_number_seq'),
    customer_id      UUID           NOT NULL,   -- user-service, không FK
    -- Marketplace: 1 lần checkout được tách thành nhiều đơn, mỗi đơn thuộc 1 merchant
    merchant_id      UUID           NOT NULL,
    checkout_id      UUID           NOT NULL,   -- nhóm các đơn cùng 1 lần thanh toán
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    payment_status   VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    currency         CHAR(3)        NOT NULL DEFAULT 'VND',
    subtotal_amount  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    discount_amount  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    shipping_amount  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    tax_amount       NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    -- Snapshot địa chỉ tại thời điểm đặt hàng (không phụ thuộc user-service sau này)
    shipping_address JSONB          NOT NULL,
    billing_address  JSONB,
    note             VARCHAR(1000),
    -- Chống tạo đơn trùng khi client retry
    idempotency_key  VARCHAR(100),
    placed_at        TIMESTAMPTZ,
    cancelled_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_orders_number UNIQUE (order_number),
    CONSTRAINT ck_orders_status CHECK (status IN
        ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_orders_payment_status CHECK (payment_status IN
        ('UNPAID', 'PAID', 'PARTIALLY_REFUNDED', 'REFUNDED', 'FAILED')),
    CONSTRAINT ck_orders_amounts CHECK (
        subtotal_amount >= 0 AND discount_amount >= 0 AND shipping_amount >= 0
            AND tax_amount >= 0 AND total_amount >= 0)
);
CREATE INDEX idx_orders_customer ON orders (customer_id, created_at DESC);
CREATE INDEX idx_orders_merchant ON orders (merchant_id, status, created_at DESC);
CREATE INDEX idx_orders_checkout ON orders (checkout_id);
CREATE INDEX idx_orders_status ON orders (status, created_at DESC);
CREATE UNIQUE INDEX uq_orders_idempotency
    ON orders (customer_id, idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE order_item
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_id     UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    -- variant_id giữ để truy vết, KHÔNG cascade: xoá sản phẩm không được làm mất lịch sử đơn
    variant_id   UUID           REFERENCES product_variant (id) ON DELETE SET NULL,
    -- Snapshot thông tin sản phẩm tại thời điểm mua
    product_id   UUID,
    sku          VARCHAR(100)   NOT NULL,
    product_name VARCHAR(500)   NOT NULL,
    variant_name VARCHAR(500),
    unit_price   NUMERIC(19, 2) NOT NULL,
    quantity     INT            NOT NULL,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    line_total   NUMERIC(19, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_order_item_qty CHECK (quantity > 0),
    CONSTRAINT ck_order_item_price CHECK (unit_price >= 0 AND discount_amount >= 0 AND line_total >= 0)
);
CREATE INDEX idx_order_item_order ON order_item (order_id);
CREATE INDEX idx_order_item_variant ON order_item (variant_id);

CREATE TABLE order_status_history
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    order_id    UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    reason      VARCHAR(500),
    changed_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_order_history_order ON order_status_history (order_id, created_at);

ALTER TABLE stock_reservation
    ADD CONSTRAINT fk_reservation_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

-- ---------------------------------------------------------------------
-- 4. CART
-- ---------------------------------------------------------------------
CREATE TABLE cart
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    customer_id BIGINT NOT NULL UNIQUE,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency    CHAR(3)     NOT NULL DEFAULT 'VND',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    version     BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT ck_cart_status CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED', 'MERGED')),
    CONSTRAINT ck_cart_owner CHECK (customer_id IS NOT NULL)
);
-- Mỗi khách chỉ có tối đa 1 giỏ ACTIVE
CREATE UNIQUE INDEX uq_cart_active_customer ON cart (customer_id)
    WHERE status = 'ACTIVE' AND customer_id IS NOT NULL;

CREATE TABLE cart_item
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    cart_id          UUID           NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    variant_id       UUID           NOT NULL REFERENCES product_variant (id) ON DELETE CASCADE,
    quantity         INT            NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_cart_item UNIQUE (cart_id, variant_id),
    CONSTRAINT ck_cart_item_qty CHECK (quantity > 0)
);
CREATE INDEX idx_cart_item_variant ON cart_item (variant_id);

-- ---------------------------------------------------------------------
-- 5. WISHLIST
-- ---------------------------------------------------------------------
CREATE TABLE wishlist
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    customer_id UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL DEFAULT 'Default',
    is_public   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_wishlist_customer_name UNIQUE (customer_id, name)
);

CREATE TABLE wishlist_item
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    wishlist_id UUID        NOT NULL REFERENCES wishlist (id) ON DELETE CASCADE,
    product_id  UUID        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    variant_id  UUID REFERENCES product_variant (id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- variant_id có thể NULL nên dùng COALESCE để unique hoạt động đúng
CREATE UNIQUE INDEX uq_wishlist_item
    ON wishlist_item (wishlist_id, product_id, COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::uuid));
CREATE INDEX idx_wishlist_item_product ON wishlist_item (product_id);

-- ---------------------------------------------------------------------
-- 6. TRANSACTIONAL OUTBOX
-- ---------------------------------------------------------------------

CREATE TABLE outbox_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type VARCHAR(30) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(id) WHERE published_at IS NULL;

CREATE TABLE processed_messages (
    message_id VARCHAR(100) PRIMARY KEY,
    consumer VARCHAR(50) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 7. updated_at triggers
-- ---------------------------------------------------------------------
DO
$$
    DECLARE
        t TEXT;
    BEGIN
        FOREACH t IN ARRAY ARRAY [
            'category', 'brand', 'product', 'product_variant',
            'warehouse', 'stock_item', 'stock_reservation',
            'orders', 'cart', 'cart_item', 'wishlist'
            ]
            LOOP
                EXECUTE format(
                        'CREATE TRIGGER trg_%1$s_updated_at BEFORE UPDATE ON %1$I
                         FOR EACH ROW EXECUTE FUNCTION set_updated_at()', t);
            END LOOP;
    END
$$;