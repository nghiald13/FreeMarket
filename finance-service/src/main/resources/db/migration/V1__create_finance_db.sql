-- ============================================================
-- 1. VÍ (WALLETS)
-- ============================================================
CREATE TABLE wallets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NULL,                       -- Auth Service accounts.id; NULL nếu là ví hệ thống
    wallet_type VARCHAR(30) NOT NULL
        CHECK (wallet_type IN ('CUSTOMER', 'MERCHANT', 'SYSTEM_ESCROW', 'SYSTEM_REVENUE')),
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    balance NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    held_balance NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (held_balance >= 0), -- tiền đang bị khóa (rút tiền chờ duyệt...)
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    version BIGINT NOT NULL DEFAULT 0,            -- optimistic locking
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_wallet_held CHECK (held_balance <= balance),
    CONSTRAINT chk_wallet_owner CHECK (
        (wallet_type IN ('CUSTOMER', 'MERCHANT') AND account_id IS NOT NULL)
        OR (wallet_type IN ('SYSTEM_ESCROW', 'SYSTEM_REVENUE') AND account_id IS NULL)
    )
);
-- Mỗi account chỉ có 1 ví / loại / tiền tệ
CREATE UNIQUE INDEX uq_wallet_account ON wallets(account_id, wallet_type, currency) WHERE account_id IS NOT NULL;
-- Mỗi loại ví hệ thống chỉ có 1 / tiền tệ
CREATE UNIQUE INDEX uq_wallet_system ON wallets(wallet_type, currency) WHERE account_id IS NULL;

-- ============================================================
-- 2. SỔ CÁI (LEDGER) - DOUBLE ENTRY, BẤT BIẾN (APPEND-ONLY)
-- ============================================================
-- Header của 1 giao dịch tài chính (tổng DEBIT = tổng CREDIT trong ledger_entries)
CREATE TABLE ledger_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(30) NOT NULL
        CHECK (type IN ('TOPUP', 'WITHDRAW', 'PAYMENT', 'ESCROW_RELEASE', 'COMMISSION',
                        'REFUND', 'TRANSFER', 'ADJUSTMENT', 'REVERSAL')),
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED'
        CHECK (status IN ('POSTED', 'REVERSED')),
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    reference_type VARCHAR(30) NULL,              -- 'ORDER', 'PAYMENT', 'REFUND', 'WITHDRAWAL'
    reference_id VARCHAR(64) NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE, -- chống ghi sổ trùng khi retry / message duplicate
    reversed_of BIGINT NULL REFERENCES ledger_transactions(id),
    description VARCHAR(255) NULL,
    metadata JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_tx_reference ON ledger_transactions(reference_type, reference_id);

-- Từng dòng bút toán: mỗi giao dịch có >= 2 dòng
CREATE TABLE ledger_entries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES ledger_transactions(id),
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    direction VARCHAR(6) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')), -- DEBIT: trừ ví, CREDIT: cộng ví
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    balance_after NUMERIC(19,2) NOT NULL,         -- snapshot số dư để đối soát / sao kê
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_entries_wallet ON ledger_entries(wallet_id, created_at DESC);
CREATE INDEX idx_ledger_entries_tx ON ledger_entries(transaction_id);

-- ============================================================
-- 3. THANH TOÁN (PAYMENTS)
-- ============================================================
-- 1 payment = 1 ý định thanh toán cho 1 order
CREATE TABLE payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_no VARCHAR(40) NOT NULL UNIQUE,       -- mã gửi cổng thanh toán / hiển thị cho khách
    order_id BIGINT NOT NULL,                     -- Order Service
    account_id BIGINT NOT NULL,                   -- người trả tiền
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    refunded_amount NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (refunded_amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    method VARCHAR(30) NOT NULL
        CHECK (method IN ('WALLET', 'COD', 'BANK_TRANSFER', 'CARD', 'VNPAY', 'MOMO', 'ZALOPAY')),
    provider VARCHAR(30) NULL,                    -- NULL nếu WALLET / COD
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PROCESSING', 'AUTHORIZED', 'SUCCEEDED', 'FAILED',
                          'CANCELLED', 'EXPIRED', 'PARTIALLY_REFUNDED', 'REFUNDED')),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    provider_txn_id VARCHAR(100) NULL,
    failure_code VARCHAR(50) NULL,
    failure_message VARCHAR(255) NULL,
    expires_at TIMESTAMPTZ NULL,
    paid_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_refund_le_amount CHECK (refunded_amount <= amount)
);
-- Mỗi order chỉ có tối đa 1 payment "đang sống" hoặc đã thành công
CREATE UNIQUE INDEX uq_payment_active_order ON payments(order_id)
    WHERE status IN ('PENDING', 'PROCESSING', 'AUTHORIZED', 'SUCCEEDED', 'PARTIALLY_REFUNDED');
CREATE INDEX idx_payments_account ON payments(account_id, created_at DESC);
CREATE INDEX idx_payments_expiry ON payments(expires_at) WHERE status = 'PENDING';

-- Các lần tương tác với cổng thanh toán (charge / capture / void) - phục vụ debug & đối soát
CREATE TABLE payment_attempts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CHARGE', 'CAPTURE', 'VOID')),
    provider VARCHAR(30) NOT NULL,
    provider_txn_id VARCHAR(100) NULL,
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
    error_code VARCHAR(50) NULL,
    request_payload JSONB NULL,                   -- không lưu số thẻ / dữ liệu nhạy cảm
    response_payload JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_attempt_provider_txn UNIQUE (provider, provider_txn_id)
);
CREATE INDEX idx_payment_attempts_payment ON payment_attempts(payment_id);

-- Webhook/IPN từ cổng thanh toán: lưu thô + chống xử lý trùng
CREATE TABLE payment_webhook_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider VARCHAR(30) NOT NULL,
    event_id VARCHAR(100) NOT NULL,               -- id sự kiện hoặc provider_txn_id
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    signature_valid BOOLEAN NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMPTZ NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_webhook_event UNIQUE (provider, event_id)
);

-- ============================================================
-- 4. HOÀN TIỀN (REFUNDS)
-- ============================================================
CREATE TABLE refunds (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    refund_no VARCHAR(40) NOT NULL UNIQUE,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    order_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    destination VARCHAR(30) NOT NULL DEFAULT 'ORIGINAL_METHOD'
        CHECK (destination IN ('ORIGINAL_METHOD', 'WALLET')),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
    reason VARCHAR(255) NULL,
    requested_by BIGINT NULL,                     -- account_id của khách/admin yêu cầu
    provider_refund_id VARCHAR(100) NULL,
    ledger_transaction_id BIGINT NULL REFERENCES ledger_transactions(id),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);

-- ============================================================
-- 5. RÚT TIỀN (WITHDRAWALS) - cho merchant / khách rút về ngân hàng
-- ============================================================
CREATE TABLE withdrawals (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    fee NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (fee >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    bank_code VARCHAR(20) NOT NULL,               -- snapshot thông tin ngân hàng tại thời điểm rút
    bank_account_no VARCHAR(30) NOT NULL,
    bank_account_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    reviewed_by BIGINT NULL,                      -- admin account_id
    reject_reason VARCHAR(255) NULL,
    ledger_transaction_id BIGINT NULL REFERENCES ledger_transactions(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_withdrawals_wallet ON withdrawals(wallet_id, created_at DESC);
CREATE INDEX idx_withdrawals_status ON withdrawals(status);

-- ============================================================
-- 6. TRANSACTIONAL OUTBOX (phát event Kafka/RabbitMQ an toàn)
-- ============================================================
-- Ghi cùng transaction DB với nghiệp vụ; worker/Debezium đọc và publish.
-- Ví dụ event: PaymentSucceeded, PaymentFailed, RefundSucceeded, WalletCredited
CREATE TABLE outbox_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type VARCHAR(30) NOT NULL,          -- 'PAYMENT', 'REFUND', 'WALLET'
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(id) WHERE published_at IS NULL;

-- ============================================================
-- 7. CONSUMER IDEMPOTENCY (chống xử lý trùng message từ service khác)
-- ============================================================
CREATE TABLE processed_messages (
    message_id VARCHAR(100) PRIMARY KEY,          -- id event nhận từ Order Service...
    consumer VARCHAR(50) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);