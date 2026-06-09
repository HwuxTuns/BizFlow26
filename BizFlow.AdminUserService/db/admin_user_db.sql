-- Extracted schema for admin user management
CREATE DATABASE IF NOT EXISTS admin_user_db;
USE admin_user_db;

DROP TABLE IF EXISTS branches;
CREATE TABLE branches (
  id bigint NOT NULL PRIMARY KEY,
  address varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  is_active bit(1) NOT NULL,
  name varchar(255) NOT NULL,
  phone varchar(255) DEFAULT NULL,
  owner_id bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO branches (id, address, email, is_active, name, phone, owner_id) VALUES
(1, 'TÂN CHÁNH HIỆP', 'gtvt@gmail.com', b'1', 'GTVT', '0981764731', NULL),
(2, '123 Street', 'sadanhthue01@gmail.com', b'1', 'Tân Bình', '0981764731', 2);

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id bigint NOT NULL PRIMARY KEY,
  username varchar(50) NOT NULL,
  password varchar(255) NOT NULL,
  email varchar(100) NOT NULL,
  full_name varchar(100) DEFAULT NULL,
  phone_number varchar(20) DEFAULT NULL,
  role varchar(20) NOT NULL,
  enabled tinyint(1) NOT NULL DEFAULT '1',
  branch_id bigint DEFAULT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  note text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO users (id, username, password, email, full_name, phone_number, role, enabled, branch_id, created_at, updated_at, note) VALUES
(1, 'admin', '$2a$10$$2b$12$BFyOXmjOjCj3bF6QXDmsHur5Zcu6uwbDpyL4EowU6TqDTgcqntWdu', 'admin@bizflow.com', 'Administrator', NULL, 'ADMIN', 1, NULL, '2025-12-21 10:47:36', '2025-12-21 10:47:36', NULL),
(2, 'owner', '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy', 'owner@bizflow.com', 'Store Owner', NULL, 'OWNER', 1, NULL, '2025-12-21 10:47:36', '2025-12-21 10:47:36', NULL),
(3, 'test', '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy', 'test@bizflow.com', 'Test User', NULL, 'EMPLOYEE', 1, NULL, '2025-12-21 10:47:36', '2025-12-21 10:47:36', NULL),
(4, 'vietphd', '$2a$10$hTmAfVr7LjuSr5AxSKrpJeleoHtsiZn1RuVH9jub038t4C5SAIhiq', 'nhanvien1@gmail.com', 'Phạm Huy Đức Việt', '0902313141', 'EMPLOYEE', 1, NULL, '2025-12-24 16:44:38', '2026-01-02 14:08:05', NULL),
(7, 'Tutl', '$2a$10$0P6niSx/VIjhEfnjFVv.cOWuRpb.WTEhAvCEdTzUO9BFyuDVwp2je', 'Tutl@gmail.com', 'Trần Long Tứ', '0866066043', 'EMPLOYEE', 1, NULL, '2026-01-03 21:57:14', '2026-01-03 21:57:14', NULL),
(8, 'TanBinh', '$2a$10$C2DybkhUAxwMFkLTSIVnteX834ZBW/Glnvg2OKPZxVzNZJvAYCIyW', 'tanbinh@gmail.com', 'Tân Bình', '0866066042', 'OWNER', 1, 2, '2026-01-21 22:25:46', '2026-01-21 22:25:46', NULL);
