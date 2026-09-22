CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- ============================================================
-- 1. BẢNG TÀI KHOẢN TRUNG TÂM (USERS / ACCOUNTS)
-- ============================================================
CREATE TABLE accounts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(150) NULL UNIQUE,
                       phone VARCHAR(20) NULL UNIQUE,
                       password VARCHAR(255) NULL, -- NULL nếu dùng OAuth2 thuần
                       status ENUM('INACTIVE', 'ACTIVE', 'SUSPENDED', 'LOCKED') DEFAULT 'INACTIVE',
                       is_mfa_enabled BOOLEAN DEFAULT FALSE,
                       mfa_secret VARCHAR(255) NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. BẢNG VAI TRÒ & QUYỀN HẠN (RBAC - ROLE BASED ACCESS CONTROL)
-- ============================================================
-- Bảng Vai trò (Roles: CUSTOMER, MERCHANT, ADMIN, SUPPORT, ...)
CREATE TABLE roles (
                       role_id INT AUTO_INCREMENT PRIMARY KEY,
                       code VARCHAR(50) NOT NULL UNIQUE, -- Ví dụ: 'ROLE_CUSTOMER', 'ROLE_MERCHANT', 'ROLE_ADMIN'
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(255) NULL
);

-- Bảng Quyền chi tiết (Permissions)
CREATE TABLE permissions (
                             permission_id INT AUTO_INCREMENT PRIMARY KEY,
                             code VARCHAR(100) NOT NULL UNIQUE, -- Ví dụ: 'product:create', 'order:cancel', 'account:block'
                             description VARCHAR(255) NULL
);

-- Bảng nối User - Role (1 User có thể có nhiều Role)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id INT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- Bảng nối Role - Permission (1 Role gồm nhiều Permission)
CREATE TABLE role_permissions (
                                  role_id INT NOT NULL,
                                  permission_id INT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- ============================================================
-- 4. BẢNG ĐĂNG NHẬP MẠNG XÃ HỘI (OAUTH2 / SOCIAL LOGIN)
-- ============================================================
CREATE TABLE social_accounts (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 account_id BIGINT NOT NULL,
                                 provider ENUM('GOOGLE', 'FACEBOOK', 'APPLE', 'GITHUB') NOT NULL,
                                 provider_account_id VARCHAR(255) NOT NULL, -- ID trả về từ Google/Facebook
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE KEY unique_provider_user (provider, provider_user_id),
                                 FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- ============================================================
-- 6. BẢNG NHẬT KÝ ĐĂNG NHẬP (AUDIT LOGS)
-- ============================================================
CREATE TABLE login_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            account_id BIGINT NULL,
                            identifier_used VARCHAR(150) NOT NULL, -- Email/Phone đã nhập
                            status ENUM('SUCCESS', 'FAILED_WRONG_PASSWORD', 'BLOCKED', 'MFA_FAILED') NOT NULL,
                            ip_address VARCHAR(45) NULL,
                            user_agent VARCHAR(255) NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Index tối ưu truy vấn
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);