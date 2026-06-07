CREATE DATABASE IF NOT EXISTS personal_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_finance;

-- ============================================================
-- 用户表（与 schema.sql 一致）
-- ============================================================
CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色: ADMIN, USER, VIEWER',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_app_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 分类表（与 schema.sql 一致）
-- ============================================================
CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL COMMENT '所属用户ID, NULL表示全局分类',
  name VARCHAR(50) NOT NULL,
  type VARCHAR(20) NOT NULL,
  sort_order INT NOT NULL DEFAULT 100,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category_type (type),
  INDEX idx_category_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 账单表（与 schema.sql 一致）
-- ============================================================
CREATE TABLE IF NOT EXISTS bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  name VARCHAR(100) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  type VARCHAR(20) NOT NULL,
  category_id BIGINT NOT NULL,
  consume_date DATE NOT NULL,
  remark VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_bill_user_id (user_id),
  INDEX idx_bill_consume_date (consume_date),
  INDEX idx_bill_category_id (category_id),
  INDEX idx_bill_type (type),
  CONSTRAINT fk_bill_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 种子数据
-- ============================================================

-- 演示用户（密码: password，BCrypt 加密，Spring Security 公开测试向量，仅用于本地演示）
INSERT INTO app_user (id, username, email, password_hash, role)
VALUES (1, 'demo', 'demo@test.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'USER')
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  email = VALUES(email),
  password_hash = VALUES(password_hash),
  role = VALUES(role);

-- 全局分类
INSERT INTO category (id, user_id, name, type, sort_order)
VALUES
  (1, NULL, 'Salary', 'income', 10),
  (2, NULL, 'Bonus', 'income', 20),
  (3, NULL, 'Food', 'expense', 10),
  (4, NULL, 'Transport', 'expense', 20),
  (5, NULL, 'Shopping', 'expense', 30),
  (6, NULL, 'Entertainment', 'expense', 40),
  (7, NULL, 'Other', 'expense', 100)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  type = VALUES(type),
  sort_order = VALUES(sort_order);

-- 演示账单（关联 demo 用户 id=1）
INSERT INTO bill (id, user_id, name, amount, type, category_id, consume_date, remark)
VALUES
  (1, 1, 'Monthly salary', 5000.00, 'income', 1, '2026-04-01', 'Base income'),
  (2, 1, 'Lunch', 38.50, 'expense', 3, '2026-04-05', 'Workday meal'),
  (3, 1, 'Metro', 8.00, 'expense', 4, '2026-04-06', 'Commute'),
  (4, 1, 'Groceries', 156.80, 'expense', 5, '2026-04-08', 'Weekly shopping'),
  (5, 1, 'Movie ticket', 45.00, 'expense', 6, '2026-04-12', 'Weekend')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  amount = VALUES(amount),
  type = VALUES(type),
  category_id = VALUES(category_id),
  consume_date = VALUES(consume_date),
  remark = VALUES(remark);
