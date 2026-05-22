CREATE DATABASE IF NOT EXISTS personal_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_finance;

CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  type VARCHAR(20) NOT NULL,
  sort_order INT NOT NULL DEFAULT 100,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  type VARCHAR(20) NOT NULL,
  category_id BIGINT NOT NULL,
  consume_date DATE NOT NULL,
  remark VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_bill_consume_date (consume_date),
  INDEX idx_bill_category_id (category_id),
  INDEX idx_bill_type (type),
  CONSTRAINT fk_bill_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_app_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO category (id, name, type, sort_order)
VALUES
  (1, 'Salary', 'income', 10),
  (2, 'Bonus', 'income', 20),
  (3, 'Food', 'expense', 10),
  (4, 'Transport', 'expense', 20),
  (5, 'Shopping', 'expense', 30),
  (6, 'Entertainment', 'expense', 40),
  (7, 'Other', 'expense', 100)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  type = VALUES(type),
  sort_order = VALUES(sort_order);

INSERT INTO bill (id, name, amount, type, category_id, consume_date, remark)
VALUES
  (1, 'Monthly salary', 5000.00, 'income', 1, '2026-04-01', 'Base income'),
  (2, 'Lunch', 38.50, 'expense', 3, '2026-04-05', 'Workday meal'),
  (3, 'Metro', 8.00, 'expense', 4, '2026-04-06', 'Commute'),
  (4, 'Groceries', 156.80, 'expense', 5, '2026-04-08', 'Weekly shopping'),
  (5, 'Movie ticket', 45.00, 'expense', 6, '2026-04-12', 'Weekend')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  amount = VALUES(amount),
  type = VALUES(type),
  category_id = VALUES(category_id),
  consume_date = VALUES(consume_date),
  remark = VALUES(remark);
