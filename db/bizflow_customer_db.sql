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
-- Database: `bizflow_customer_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `cccd` varchar(255) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `monthly_points` int NOT NULL,
  `tier` enum('BAC','BACH_KIM','DONG','KIM_CUONG','VANG') DEFAULT NULL,
  `total_points` int NOT NULL,
  `gender` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`id`, `address`, `email`, `name`, `phone`, `cccd`, `dob`, `monthly_points`, `tier`, `total_points`, `gender`) VALUES
(1, 'tân bình', NULL, 'Pham viet', '0866066042', NULL, NULL, 44, NULL, 44, NULL),
(2, 'tân bình', NULL, 'Anh Thới', '0866066043', NULL, NULL, 7518, NULL, 7518, NULL),
(5, 'chung cư', NULL, 'Anh Tú', '0866066044', NULL, NULL, 955, NULL, 955, NULL),
(6, 'tân bình', NULL, 'Chị Vân', '0866066045', NULL, NULL, 243, 'DONG', 243, NULL),
(7, 'Test Address', NULL, 'Test Customer', '0962028826', NULL, NULL, 0, 'DONG', 0, NULL),
(8, 'Test Address', NULL, 'Test Customer 2', '0928519177', NULL, NULL, 10, 'DONG', 10, NULL),
(10, 'Test Address', NULL, 'Test UI Flow', '0996622189', NULL, NULL, 0, 'DONG', 0, NULL),
(11, 'Tân Chánh Hiệp', NULL, 'Anh Trung', '0354970825', NULL, NULL, 0, 'DONG', 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `point_history`
--

CREATE TABLE `point_history` (
  `id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `points` int DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `point_history`
--

INSERT INTO `point_history` (`id`, `created_at`, `points`, `reason`, `customer_id`) VALUES
(2, '2026-01-11 22:29:34.759798', 10, 'ORDER_10', 8);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `point_history`
--
ALTER TABLE `point_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKoykoexosmdcwsmph7ubqmq22` (`customer_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `point_history`
--
ALTER TABLE `point_history`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `point_history`
--
ALTER TABLE `point_history`
  ADD CONSTRAINT `FKoykoexosmdcwsmph7ubqmq22` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
