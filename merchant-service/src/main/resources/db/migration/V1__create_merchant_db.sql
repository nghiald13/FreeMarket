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

-- ---------------------------------------------------------------------
-- 1. MERCHANT (gian hàng / người bán)
-- ---------------------------------------------------------------------
-- merchants.id (UUID) chính là merchant_id mà inventory-service đang dùng
-- (Product.merchantId, Warehouse.merchantId, Order.merchantId).
-- account_id (BIGINT) liên kết với auth-service accounts.id, không FK vì khác service.
CREATE TABLE merchants
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    account_id     BIGINT       NOT NULL,   -- auth-service accounts.id
    shop_name      VARCHAR(255) NOT NULL,
    slug           VARCHAR(255) NOT NULL,
    description    TEXT,
    logo_url       VARCHAR(1024),
    banner_url     VARCHAR(1024),
    contact_email  VARCHAR(150),
    contact_phone  VARCHAR(20),
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    status_reason  VARCHAR(500),            -- lý do từ chối / tạm khóa gần nhất
    reviewed_by    BIGINT,                  -- admin account_id duyệt gần nhất
    reviewed_at    TIMESTAMPTZ,
    approved_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_merchants_account UNIQUE (account_id),   -- 1 account chỉ có 1 shop
    CONSTRAINT uq_merchants_slug UNIQUE (slug),
    CONSTRAINT uq_merchants_shop_name UNIQUE (shop_name),
    CONSTRAINT ck_merchants_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT ck_merchants_approved_at CHECK (status <> 'APPROVED' OR approved_at IS NOT NULL)
);
CREATE INDEX idx_merchants_status ON merchants (status, created_at DESC);

-- ---------------------------------------------------------------------
-- 2. ĐỊA CHỈ (lấy hàng / trả hàng / kho)
-- ---------------------------------------------------------------------
CREATE TABLE merchant_addresses
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    merchant_id   UUID         NOT NULL REFERENCES merchants (id) ON DELETE CASCADE,
    label         VARCHAR(100),                       -- ví dụ: 'Kho chính', 'Cửa hàng Q1'
    contact_name  VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20)  NOT NULL,
    province      VARCHAR(100) NOT NULL,
    district      VARCHAR(100) NOT NULL,
    ward          VARCHAR(100) NOT NULL,
    address_line  VARCHAR(255) NOT NULL,
    latitude      NUMERIC(9, 6),
    longitude     NUMERIC(9, 6),
    is_pickup     BOOLEAN      NOT NULL DEFAULT TRUE,  -- địa chỉ lấy hàng
    is_return     BOOLEAN      NOT NULL DEFAULT FALSE, -- địa chỉ nhận hàng trả
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_merchant_address_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_merchant_address_lng CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);
CREATE INDEX idx_merchant_addresses_merchant ON merchant_addresses (merchant_id);
-- Mỗi merchant chỉ có tối đa 1 địa chỉ mặc định
CREATE UNIQUE INDEX uq_merchant_one_default_address
    ON merchant_addresses (merchant_id) WHERE is_default;

-- ---------------------------------------------------------------------
-- 3. THÔNG TIN PHÁP LÝ (KYC)
-- ---------------------------------------------------------------------
-- Dữ liệu nhạy cảm: nên mã hóa ở tầng ứng dụng (id_number) và chỉ lưu URL
-- tới storage riêng tư cho các giấy tờ.
CREATE TABLE merchant_legal_info
(
    id                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    merchant_id          UUID         NOT NULL REFERENCES merchants (id) ON DELETE CASCADE,
    business_type        VARCHAR(20)  NOT NULL,       -- INDIVIDUAL: cá nhân, COMPANY: doanh nghiệp / hộ kinh doanh
    legal_name           VARCHAR(255) NOT NULL,       -- họ tên chủ shop hoặc tên công ty
    tax_code             VARCHAR(20),
    business_license_no  VARCHAR(50),
    business_license_url VARCHAR(1024),
    id_number            VARCHAR(255) NOT NULL,       -- CCCD/CMND/hộ chiếu (nên lưu dạng mã hóa)
    id_front_url         VARCHAR(1024) NOT NULL,
    id_back_url          VARCHAR(1024),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version              BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_merchant_legal_merchant UNIQUE (merchant_id),   -- 1 hồ sơ / merchant
    CONSTRAINT ck_merchant_legal_type CHECK (business_type IN ('INDIVIDUAL', 'COMPANY')),
    CONSTRAINT ck_merchant_legal_company CHECK (
        business_type = 'INDIVIDUAL' OR (tax_code IS NOT NULL AND business_license_no IS NOT NULL)
    )
);

-- ---------------------------------------------------------------------
-- 4. TÀI KHOẢN NGÂN HÀNG (nhận tiền rút từ ví)
-- ---------------------------------------------------------------------
CREATE TABLE merchant_bank_accounts
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    merchant_id       UUID         NOT NULL REFERENCES merchants (id) ON DELETE CASCADE,
    bank_code         VARCHAR(20)  NOT NULL,
    bank_account_no   VARCHAR(30)  NOT NULL,
    bank_account_name VARCHAR(100) NOT NULL,
    branch            VARCHAR(255),
    is_default        BOOLEAN      NOT NULL DEFAULT FALSE,
    is_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_merchant_bank_account UNIQUE (merchant_id, bank_code, bank_account_no)
);
CREATE INDEX idx_merchant_bank_merchant ON merchant_bank_accounts (merchant_id);
CREATE UNIQUE INDEX uq_merchant_one_default_bank
    ON merchant_bank_accounts (merchant_id) WHERE is_default;

-- ---------------------------------------------------------------------
-- 5. LỊCH SỬ TRẠNG THÁI (audit, append-only)
-- ---------------------------------------------------------------------
CREATE TABLE merchant_status_history
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    merchant_id UUID        NOT NULL REFERENCES merchants (id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    reason      VARCHAR(500),
    changed_by  BIGINT,                  -- account_id của admin / chính merchant / NULL nếu hệ thống
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_merchant_history_from CHECK (from_status IS NULL OR from_status IN
        ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT ck_merchant_history_to CHECK (to_status IN
        ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED'))
);
CREATE INDEX idx_merchant_history_merchant ON merchant_status_history (merchant_id, created_at);

-- ---------------------------------------------------------------------
-- 6. TRANSACTIONAL OUTBOX (merchant.approved, merchant.suspended, ...)
-- ---------------------------------------------------------------------
CREATE TABLE outbox_events
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type VARCHAR(30) NOT NULL,
    aggregate_id   VARCHAR(64) NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload        JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_unpublished ON outbox_events (id) WHERE published_at IS NULL;

CREATE TABLE processed_messages
(
    message_id   VARCHAR(100) PRIMARY KEY,
    consumer     VARCHAR(50) NOT NULL,
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
            'merchants', 'merchant_addresses', 'merchant_legal_info', 'merchant_bank_accounts'
            ]
            LOOP
                EXECUTE format(
                        'CREATE TRIGGER trg_%1$s_updated_at BEFORE UPDATE ON %1$I
                         FOR EACH ROW EXECUTE FUNCTION set_updated_at()', t);
            END LOOP;
    END
$$;