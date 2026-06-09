-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: mysql:3306
-- Generation Time: Feb 03, 2026 at 02:33 PM
-- Server version: 8.0.45
-- PHP Version: 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bizflow_auth_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `branches`
--

CREATE TABLE `branches` (
  `id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `branches`
--

INSERT INTO `branches` (`id`, `address`, `email`, `is_active`, `name`, `phone`, `owner_id`) VALUES
(1, 'TÂN CHÁNH HIỆP', 'gtvt@gmail.com', b'1', 'GTVT', '0981764731', NULL),
(2, '123 Street', 'sadanhthue01@gmail.com', b'1', 'Tân Bình', '0981764731', 2);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `branch_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `note` text,
  `password_hash` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `email`, `full_name`, `phone_number`, `role`, `enabled`, `branch_id`, `created_at`, `updated_at`, `note`, `password_hash`) VALUES
(1, 'admin', '$2a$10$7gz3idM0iA0ikYyibDutqe31yrWDdVh2NIRa1gCj0QXVNw9723f0G', 'admin@bizflow.com', 'Administrator', NULL, 'ADMIN', 1, NULL, '2025-12-21 10:47:36', '2026-02-03 14:09:53', NULL, '$2a$10$7gz3idM0iA0ikYyibDutqe31yrWDdVh2NIRa1gCj0QXVNw9723f0G'),
(2, 'owner', '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy', 'owner@bizflow.com', 'Store Owner', NULL, 'OWNER', 1, NULL, '2025-12-21 10:47:36', '2026-02-03 14:09:53', NULL, '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy'),
(3, 'test', '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy', 'test@bizflow.com', 'Test User', NULL, 'EMPLOYEE', 1, NULL, '2025-12-21 10:47:36', '2026-02-03 14:09:53', NULL, '$2a$10$iDS5.CarVV4hxkD1P5oVYePzl/M8gs3jse7bGOAjhQBZ6iefSllWy'),
(4, 'vietphd', '$2a$10$hTmAfVr7LjuSr5AxSKrpJeleoHtsiZn1RuVH9jub038t4C5SAIhiq', 'nhanvien1@gmail.com', 'Phạm Huy Đức Việt', '0902313141', 'EMPLOYEE', 1, NULL, '2025-12-24 16:44:38', '2026-02-03 14:09:53', NULL, '$2a$10$hTmAfVr7LjuSr5AxSKrpJeleoHtsiZn1RuVH9jub038t4C5SAIhiq'),
(7, 'Tutl', '$2a$10$0P6niSx/VIjhEfnjFVv.cOWuRpb.WTEhAvCEdTzUO9BFyuDVwp2je', 'Tutl@gmail.com', 'Trần Long Tú', '0866066043', 'EMPLOYEE', 1, NULL, '2026-01-03 21:57:14', '2026-02-03 14:09:53', NULL, '$2a$10$0P6niSx/VIjhEfnjFVv.cOWuRpb.WTEhAvCEdTzUO9BFyuDVwp2je'),
(8, 'TanBinh', '$2a$10$C2DybkhUAxwMFkLTSIVnteX834ZBW/Glnvg2OKPZxVzNZJvAYCIyW', 'tanbinh@gmail.com', 'Tấn Bình', '0866066042', 'OWNER', 1, 2, '2026-01-21 22:25:46', '2026-02-03 14:09:53', NULL, '$2a$10$C2DybkhUAxwMFkLTSIVnteX834ZBW/Glnvg2OKPZxVzNZJvAYCIyW'),
(9, 'admin_tuan', '$2a$10$BWk2vGVqgLlmJTQq7LigSObKFjeXPY6Olq5BhdsAM2tCYb.Tdaq2.', 'admin.tuan@bizflow.com', 'Admin Tuan', '0909999888', 'ADMIN', 1, NULL, '2026-04-10 22:00:00', '2026-04-10 22:00:00', 'Created for admin product management', '$2a$10$BWk2vGVqgLlmJTQq7LigSObKFjeXPY6Olq5BhdsAM2tCYb.Tdaq2.');

--
-- Indexes for dumped tables
-- Indexes for table `branches`

ALTER TABLE `branches`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_hw68nd07qk3jrjfg70qxq9vb7` (`name`),
  ADD KEY `FK8lecw87wgj5h4k0x8klg4bnep` (`owner_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone_number` (`phone_number`),
  ADD KEY `fk_user_branch` (`branch_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `branches`
--
ALTER TABLE `branches`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `branches`
--
ALTER TABLE `branches`
  ADD CONSTRAINT `FK8lecw87wgj5h4k0x8klg4bnep` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `FK9o70sp9ku40077y38fk4wieyk` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
