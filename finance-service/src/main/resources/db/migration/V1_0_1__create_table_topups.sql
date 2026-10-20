create table topups (
    id bigint generated always as identity primary key,
    wallet_id bigint not null references wallets(id),
    amount numeric(19,2) not null check (amount > 0),
    currency varchar(3) not null default 'VND',
    method varchar(30) not null,
    provider varchar(30) null,
    provider_ref_id varchar(100) not null unique,
    provider_txn_id varchar(100) unique,
    status varchar(30) not null default 'PENDING'
        check (status in ('PENDING', 'SUCCESS', 'FAILED')),
    ledger_transaction_id bigint unique references ledger_transactions(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- Alter Table wallets
alter table wallets drop constraint if exists wallets_wallet_type_check;
alter table wallets drop constraint if exists wallets_balance_check;
alter table wallets drop constraint if exists chk_wallet_owner;
alter table wallets drop constraint if exists chk_wallet_held;

alter table wallets add constraint wallets_wallet_type_check
    check (wallet_type in ('CUSTOMER', 'MERCHANT', 'SYSTEM_ESCROW', 'SYSTEM_REVENUE', 'SYSTEM_CLEARING'));

alter table wallets add constraint wallets_balance_check
    check (wallet_type = 'SYSTEM_CLEARING' or balance >= 0);

alter table wallets add constraint chk_wallet_owner check (
    (wallet_type in ('CUSTOMER', 'MERCHANT') and account_id is not null)
    or (wallet_type in ('SYSTEM_ESCROW', 'SYSTEM_REVENUE', 'SYSTEM_CLEARING') and account_id is null)
);

alter table wallets add constraint chk_wallet_held
    check (wallet_type = 'SYSTEM_CLEARING' or held_balance <= balance);

-- Alter Table payments
alter table payments
rename column idempotency_key to provider_ref_id;