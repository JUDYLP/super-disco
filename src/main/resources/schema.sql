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
