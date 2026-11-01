ALTER TABLE warehouse
    ADD COLUMN owner_type  VARCHAR(20) NOT NULL DEFAULT 'MERCHANT',
    ADD COLUMN merchant_id UUID,
    ADD CONSTRAINT ck_warehouse_owner CHECK (
        (owner_type = 'MERCHANT' AND merchant_id IS NOT NULL) OR
        (owner_type = 'PLATFORM' AND merchant_id IS NULL)
    );
CREATE INDEX idx_warehouse_merchant ON warehouse (merchant_id) WHERE merchant_id IS NOT NULL;