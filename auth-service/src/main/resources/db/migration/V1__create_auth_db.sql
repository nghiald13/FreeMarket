-- ============================================================
-- 1. BẢNG TÀI KHOẢN TRUNG TÂM (USERS / ACCOUNTS)
-- ============================================================
CREATE TABLE accounts (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(40) NOT NULL,
                       email VARCHAR(150) NULL UNIQUE,
                       phone VARCHAR(20) NULL UNIQUE,
                       password VARCHAR(255) NULL, -- NULL nếu dùng OAuth2 thuần
                       status VARCHAR(30) NOT NULL DEFAULT 'INACTIVE' CHECK ( status IN ('INACTIVE', 'ACTIVE', 'SUSPENDED', 'LOCKED') ),
                       is_mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                       mfa_secret VARCHAR(255) NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ============================================================
-- 2. BẢNG VAI TRÒ & QUYỀN HẠN (RBAC - ROLE BASED ACCESS CONTROL)
-- ============================================================
-- Bảng Vai trò (Roles: CUSTOMER, MERCHANT, ADMIN, SUPPORT, ...)
CREATE TABLE roles (
                        id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       code VARCHAR(50) NOT NULL UNIQUE, -- Ví dụ: 'ROLE_CUSTOMER', 'ROLE_MERCHANT', 'ROLE_ADMIN'
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(255) NULL
);

-- Bảng Quyền chi tiết (Permissions)
CREATE TABLE permissions (
                             id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             code VARCHAR(100) NOT NULL UNIQUE, -- Ví dụ: 'product:create', 'order:cancel', 'account:block'
                             description VARCHAR(255) NULL
);

-- Bảng nối User - Role (1 User có thể có nhiều Role)
CREATE TABLE account_role (
                            account_id BIGINT NOT NULL,
                            role_id SMALLINT NOT NULL,
                            PRIMARY KEY (account_id, role_id),
                            FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Bảng nối Role - Permission (1 Role gồm nhiều Permission)
CREATE TABLE role_permissions (
                                  role_id SMALLINT NOT NULL,
                                  permission_id SMALLINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ============================================================
-- 4. BẢNG ĐĂNG NHẬP MẠNG XÃ HỘI (OAUTH2 / SOCIAL LOGIN)
-- ============================================================
CREATE TABLE social_accounts (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 account_id BIGINT NOT NULL,
                                 provider VARCHAR(30) NOT NULL CHECK ( provider IN ('GOOGLE', 'FACEBOOK', 'APPLE', 'GITHUB') ),
                                 provider_account_id VARCHAR(255) NOT NULL, -- ID trả về từ Google/Facebook
                                 created_at TIMESTAMP NOT NULL DEFAULT now(),
                                 CONSTRAINT uq_social_provider_account UNIQUE (provider, provider_account_id),
                                 CONSTRAINT fk_social_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- ============================================================
-- 6. BẢNG NHẬT KÝ ĐĂNG NHẬP (AUDIT LOGS)
-- ============================================================
CREATE TABLE login_logs (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            account_id BIGINT NULL,
                            identifier_used VARCHAR(150) NOT NULL, -- Email/Phone đã nhập
                            status VARCHAR(30) NOT NULL,
                            ip_address VARCHAR(45) NULL,
                            user_agent VARCHAR(255) NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Index tối ưu truy vấn
CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_accounts_phone ON accounts(phone);