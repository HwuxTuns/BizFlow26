-- Flyway migration: create customers and point_history tables
CREATE TABLE IF NOT EXISTS customers (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(255),
  email VARCHAR(255),
  address VARCHAR(255),
  user_id BIGINT DEFAULT NULL,
  username VARCHAR(255) DEFAULT NULL,
  total_points INT NOT NULL DEFAULT 0,
  monthly_points INT NOT NULL DEFAULT 0,
  tier VARCHAR(50) DEFAULT 'DONG',
  dob DATE DEFAULT NULL,
  cccd VARCHAR(255) DEFAULT NULL,
  gender VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS point_history (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT,
  points INT,
  reason VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_point_history_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
