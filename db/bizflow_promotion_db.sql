-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: mysql:3306
-- Generation Time: Feb 03, 2026 at 02:34 PM
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
-- Database: `bizflow_promotion_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `bundle_items`
--

CREATE TABLE `bundle_items` (
  `bundle_id` bigint NOT NULL COMMENT 'Kh?a ch?nh, m? duy nh?t c?a lu?t t?ng/combo',
  `promotion_id` bigint NOT NULL COMMENT 'Kh?a ngo?i, li?n k?t v?i promotions',
  `main_product_id` bigint NOT NULL,
  `main_quantity` int NOT NULL DEFAULT '1' COMMENT 'S? l??ng s?n ph?m ch?nh c?n mua',
  `gift_product_id` bigint NOT NULL,
  `gift_quantity` int NOT NULL DEFAULT '1' COMMENT 'S? l??ng qu? t?ng/s?n ph?m ?i k?m',
  `gift_discount_type` varchar(255) NOT NULL,
  `gift_discount_value` double NOT NULL,
  `status` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='B?ng ??nh ngh?a quy t?c Mua X t?ng Y ho?c Combo s?n ph?m';

--
-- Dumping data for table `bundle_items`
--

INSERT INTO `bundle_items` (`bundle_id`, `promotion_id`, `main_product_id`, `main_quantity`, `gift_product_id`, `gift_quantity`, `gift_discount_type`, `gift_discount_value`, `status`, `created_at`, `product_id`, `quantity`) VALUES
(36, 2036, 153, 1, 153, 1, 'FREE', 0, 'ACTIVE', '2026-01-25 07:11:26', 153, 1),
(37, 2039, 3, 3, 3, 1, 'FREE', 0, 'ACTIVE', '2026-01-25 07:37:36', 3, 3),
(38, 2041, 5, 3, 5, 1, 'FREE', 0, 'ACTIVE', '2026-01-25 07:40:41', 5, 3),
(39, 2043, 23, 3, 23, 1, 'FREE', 0, 'ACTIVE', '2026-01-25 08:30:39', 23, 3),
(40, 2045, 19, 3, 18, 1, 'FREE', 0, 'ACTIVE', '2026-01-25 10:16:18', 19, 3),
(41, 2055, 2001, 2, 2002, 1, 'PERCENT', 100, 'active', '2026-01-26 06:27:54', 2001, 2),
(42, 2055, 2001, 2, 2003, 1, 'FIXED', 0, 'active', '2026-01-26 06:27:54', 2003, 1),
(43, 2060, 36, 3, 36, 1, 'FREE', 0, 'ACTIVE', '2026-01-27 12:54:25', 36, 3),
(44, 2064, 1, 1, 1, 1, 'FREE', 0, 'ACTIVE', '2026-01-28 07:41:55', 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `promotion_id` bigint NOT NULL COMMENT 'Kh?a ch?nh, m? duy nh?t c?a ch??ng tr?nh khuy?n m?i',
  `name` varchar(255) NOT NULL COMMENT 'T?n ch??ng tr?nh (VD: Black Friday Sale, Khai tr??ng)',
  `description` text COMMENT 'M? t? chi ti?t ch??ng tr?nh',
  `promotion_type` varchar(255) DEFAULT NULL,
  `discount_value` double DEFAULT NULL,
  `start_date` datetime NOT NULL COMMENT 'Th?i gian b?t ??u ?p d?ng',
  `end_date` datetime NOT NULL COMMENT 'Th?i gian k?t th?c',
  `applies_to` enum('ALL','PRODUCTS','CATEGORIES','CUSTOMERS') NOT NULL DEFAULT 'ALL' COMMENT '?p d?ng cho: T?t c?, S?n ph?m c? th?, Danh m?c c? th?, Kh?ch h?ng c? th?',
  `status` enum('ACTIVE','INACTIVE','EXPIRED','PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Tr?ng th?i c?a ch??ng tr?nh',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active` bit(1) NOT NULL,
  `code` varchar(255) NOT NULL,
  `discount_type` enum('BUNDLE','FIXED','FIXED_AMOUNT','FREE_GIFT','PERCENT') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='B?ng l?u tr? c?c ch??ng tr?nh khuy?n m?i';

--
-- Dumping data for table `promotions`
--

INSERT INTO `promotions` (`promotion_id`, `name`, `description`, `promotion_type`, `discount_value`, `start_date`, `end_date`, `applies_to`, `status`, `created_at`, `updated_at`, `active`, `code`, `discount_type`) VALUES
(2035, 'Dale hời cuối năm', NULL, 'FIXED_AMOUNT', 2000, '2026-01-25 20:53:00', '2026-02-06 20:53:00', 'ALL', 'PENDING', '2026-01-25 06:53:29', '2026-01-27 13:28:54', b'1', 'KMDHCN-001', 'FIXED'),
(2036, 'compo', NULL, 'BUNDLE', 0, '2026-01-25 21:11:00', '2026-02-05 21:11:00', 'ALL', 'PENDING', '2026-01-25 07:11:26', '2026-01-25 07:11:26', b'1', 'KMCOMP-001', 'BUNDLE'),
(2038, 'giảm tiền sản phẩm', NULL, 'FIXED_AMOUNT', 5000, '2026-01-25 21:35:00', '2026-02-21 21:35:00', 'ALL', 'PENDING', '2026-01-25 07:36:05', '2026-01-27 08:36:31', b'1', 'KMGTSP-001', 'FIXED'),
(2039, 'compo nước', NULL, 'BUNDLE', 0, '2026-01-25 21:37:00', '2026-02-07 21:37:00', 'ALL', 'PENDING', '2026-01-25 07:37:36', '2026-01-27 08:36:31', b'1', 'KMCN-001', 'BUNDLE'),
(2040, 'bcs', 'bcs', 'FIXED_AMOUNT', 6000, '2026-01-25 21:38:00', '2026-02-07 21:38:00', 'ALL', 'PENDING', '2026-01-25 07:39:14', '2026-01-25 07:39:14', b'1', 'KMBCS-001', 'FIXED'),
(2041, 'compo nước giải khát', NULL, 'BUNDLE', 0, '2026-01-25 21:40:00', '2026-02-06 21:40:00', 'ALL', 'PENDING', '2026-01-25 07:40:41', '2026-01-27 12:41:18', b'0', 'KMCNGK-002', 'BUNDLE'),
(2042, 'giảm giá poca', NULL, 'FIXED_AMOUNT', 5000, '2026-01-25 21:44:00', '2026-02-07 21:44:00', 'ALL', 'PENDING', '2026-01-25 07:44:50', '2026-01-27 08:36:31', b'1', 'KMGGP-001', 'FIXED'),
(2043, 'Thái dúi', NULL, 'BUNDLE', 0, '2026-01-25 22:30:00', '2026-02-14 22:30:00', 'ALL', 'PENDING', '2026-01-25 08:30:39', '2026-01-27 08:50:57', b'1', 'KMTD-001', 'BUNDLE'),
(2044, 'giảm tiền sản phẩm', NULL, 'FIXED_AMOUNT', 4000, '2026-01-25 22:31:00', '2026-02-06 22:31:00', 'ALL', 'PENDING', '2026-01-25 08:32:04', '2026-01-27 08:36:31', b'1', 'KMGTSP-002', 'FIXED'),
(2045, 'Combo Siêu Tiết Kiệm - Tháng 1 2026', 'Mua combo sản phẩm với giá ưu đãi đặc biệt. Nhanh tay đặt hàng ngay hôm nay!', 'BUNDLE', 0, '2026-01-26 00:15:00', '2026-02-08 00:15:00', 'ALL', 'PENDING', '2026-01-25 10:16:18', '2026-01-27 08:50:57', b'1', 'COMBO-JAN26', 'BUNDLE'),
(2046, 'Flash Sale 15% - Tháng 1 2026', 'Giảm giá 15% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 15, '2026-01-26 00:16:00', '2026-02-14 00:16:00', 'ALL', 'PENDING', '2026-01-25 10:17:09', '2026-01-27 08:50:57', b'1', 'SALE15-JAN26', 'PERCENT'),
(2047, 'Flash Sale 25% - Tháng 1 2026', 'Giảm giá 25% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 25, '2026-01-26 02:04:00', '2026-02-28 02:05:00', 'ALL', 'PENDING', '2026-01-25 12:06:02', '2026-01-27 08:50:57', b'1', 'SALE25-JAN26', 'PERCENT'),
(2048, 'Flash Sale 40% - Tháng 1 2026', 'Giảm giá 40% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 40, '2026-01-26 02:07:00', '2026-02-08 02:07:00', 'ALL', 'PENDING', '2026-01-25 12:08:08', '2026-01-27 12:41:35', b'1', 'SALE40-JAN26', 'PERCENT'),
(2049, 'Flash Sale 20% - Tháng 1 2026', 'Giảm giá 20% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 20, '2026-01-26 02:29:00', '2026-02-13 02:29:00', 'ALL', 'PENDING', '2026-01-25 12:30:06', '2026-01-27 08:50:57', b'1', 'SALE20-JAN26', 'PERCENT'),
(2050, 'Flash Sale 10% - Tháng 1 2026', 'Giảm giá 10% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 10, '2026-01-26 03:42:00', '2026-02-07 03:43:00', 'ALL', 'PENDING', '2026-01-25 13:43:17', '2026-01-27 08:50:57', b'1', 'SALE10-JAN26', 'PERCENT'),
(2051, 'Giảm Ngay 3.000đ - Tháng 1 2026', 'Giảm ngay 3.000đ khi mua sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'FIXED_AMOUNT', 3000, '2026-01-26 04:42:00', '2026-02-08 04:42:00', 'ALL', 'PENDING', '2026-01-25 14:42:33', '2026-01-27 08:50:57', b'1', 'GIAM3K-JAN26', 'FIXED'),
(2052, 'Flash Sale 5% - Tháng 1 2026', 'Giảm giá 5% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 5, '2026-01-26 06:05:00', '2026-02-15 06:05:00', 'ALL', 'PENDING', '2026-01-25 16:05:45', '2026-01-27 08:50:57', b'1', 'SALE5-JAN26', 'PERCENT'),
(2053, 'NiFi Test Promotion 10%', 'Test promotion for NiFi flow validation', NULL, 10, '2026-01-26 00:00:00', '2026-02-26 23:59:59', 'ALL', 'PENDING', '2026-01-26 06:27:54', '2026-01-26 06:27:54', b'1', 'NIFI_TEST_01', 'PERCENT'),
(2054, 'NiFi Test Fixed 50k', 'Test fixed discount', 'FIXED_AMOUNT', 5000, '2026-01-26 00:00:00', '2026-03-26 23:59:00', 'ALL', 'PENDING', '2026-01-26 06:27:54', '2026-01-28 04:44:37', b'1', 'NIFI_TEST_02', 'FIXED'),
(2055, 'NiFi Test Bundle Deal', 'Test bundle promotion', NULL, 0, '2026-01-26 00:00:00', '2026-02-28 23:59:59', 'ALL', 'PENDING', '2026-01-26 06:27:54', '2026-01-27 10:24:17', b'1', 'NIFI_TEST_03', 'BUNDLE'),
(2057, 'Flash Sale 10% - Tháng 1 2026', 'Giảm giá 10% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 10, '2026-01-28 02:29:00', '2026-02-28 02:29:00', 'ALL', 'PENDING', '2026-01-27 12:30:03', '2026-01-27 12:30:03', b'1', 'SALE10-JAN26-79X4', 'PERCENT'),
(2058, 'Flash Sale 15% - Tháng 1 2026', 'Giảm giá 15% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 15, '2026-01-28 02:30:00', '2026-02-20 02:30:00', 'ALL', 'PENDING', '2026-01-27 12:30:30', '2026-01-27 12:50:41', b'0', 'SALE15-JAN26-VYJU', 'PERCENT'),
(2059, 'Giảm Ngay 2.000đ - Tháng 1 2026', 'Giảm ngay 2.000đ khi mua sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'FIXED_AMOUNT', 2000, '2026-01-28 02:52:00', '2026-02-15 02:52:00', 'ALL', 'PENDING', '2026-01-27 12:52:52', '2026-01-27 13:24:21', b'0', 'GIAM2K-JAN26-VS9R', 'FIXED'),
(2060, 'Combo Siêu Tiết Kiệm - Tháng 1 2026', 'Mua combo sản phẩm với giá ưu đãi đặc biệt. Nhanh tay đặt hàng ngay hôm nay!', 'BUNDLE', 0, '2026-01-28 02:53:00', '2026-02-14 02:53:00', 'ALL', 'PENDING', '2026-01-27 12:54:25', '2026-01-27 13:24:18', b'1', 'COMBO-JAN26-VBN1', 'BUNDLE'),
(2063, 'Flash Sale 5% - Tháng 1 2026', 'Giảm giá 5% cho sản phẩm được chọn. Nhanh tay đặt hàng ngay hôm nay!', 'PERCENT', 5, '2026-01-28 03:29:00', '2026-03-13 03:29:00', 'ALL', 'PENDING', '2026-01-27 13:29:30', '2026-01-27 13:29:30', b'1', 'SALE5-JAN26-PQYA', 'PERCENT'),
(2064, 'Combo Siêu Tiết Kiệm - Tháng 1 2026', 'Mua combo sản phẩm với giá ưu đãi đặc biệt. Nhanh tay đặt hàng ngay hôm nay!', 'BUNDLE', 0, '2026-01-29 21:41:00', '2026-01-31 21:41:00', 'ALL', 'PENDING', '2026-01-28 07:41:55', '2026-01-28 07:41:55', b'1', 'COMBO-JAN26-XRI5', 'BUNDLE');

-- --------------------------------------------------------

--
-- Table structure for table `promotion_targets`
--

CREATE TABLE `promotion_targets` (
  `promo_target_id` bigint NOT NULL COMMENT 'Kh?a ch?nh',
  `promotion_id` bigint NOT NULL COMMENT 'Kh?a ngo?i, li?n k?t v?i b?ng promotions',
  `product_id` int DEFAULT NULL COMMENT 'Kh?a ngo?i, li?n k?t v?i products.product_id (S?n ph?m ???c ?p d?ng)',
  `category_id` bigint DEFAULT NULL,
  `min_order_value` decimal(15,2) DEFAULT '0.00' COMMENT 'Gi? tr? ??n h?ng t?i thi?u ?? ?p d?ng (n?u c?n)',
  `max_discount_amount` decimal(15,2) DEFAULT NULL COMMENT 'S? ti?n gi?m t?i ?a (n?u l? gi?m %)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `target_id` bigint DEFAULT NULL,
  `target_type` enum('BRANCH','CATEGORY','CUSTOMER_GROUP','PRODUCT') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `promotion_targets`
--

INSERT INTO `promotion_targets` (`promo_target_id`, `promotion_id`, `product_id`, `category_id`, `min_order_value`, `max_discount_amount`, `created_at`, `target_id`, `target_type`) VALUES
(54, 2040, NULL, NULL, 0.00, NULL, '2026-01-25 07:39:14', 154, 'PRODUCT'),
(56, 2042, NULL, NULL, 0.00, NULL, '2026-01-25 08:29:25', 62, 'PRODUCT'),
(57, 2044, NULL, NULL, 0.00, NULL, '2026-01-25 08:32:04', 110, 'PRODUCT'),
(58, 2046, NULL, NULL, 0.00, NULL, '2026-01-25 10:17:09', 26, 'PRODUCT'),
(68, 2047, NULL, NULL, 0.00, NULL, '2026-01-25 12:27:50', 127, 'PRODUCT'),
(69, 2047, NULL, NULL, 0.00, NULL, '2026-01-25 12:27:50', 92, 'PRODUCT'),
(71, 2049, NULL, NULL, 0.00, NULL, '2026-01-25 12:30:46', 106, 'PRODUCT'),
(72, 2048, NULL, NULL, 0.00, NULL, '2026-01-25 13:29:14', 10, 'PRODUCT'),
(73, 2048, NULL, NULL, 0.00, NULL, '2026-01-25 13:29:14', 27, 'PRODUCT'),
(74, 2050, NULL, NULL, 0.00, NULL, '2026-01-25 13:43:17', 29, 'PRODUCT'),
(75, 2051, NULL, NULL, 0.00, NULL, '2026-01-25 14:42:33', 48, 'PRODUCT'),
(76, 2052, NULL, NULL, 0.00, NULL, '2026-01-25 16:05:45', 155, 'PRODUCT'),
(77, 2053, NULL, NULL, 0.00, NULL, '2026-01-26 06:27:54', 1001, 'PRODUCT'),
(78, 2053, NULL, NULL, 0.00, NULL, '2026-01-26 06:27:54', 1002, 'PRODUCT'),
(80, 2055, NULL, NULL, 0.00, NULL, '2026-01-26 06:27:54', 2001, 'PRODUCT'),
(82, 2057, NULL, NULL, 0.00, NULL, '2026-01-27 12:30:03', 1, 'PRODUCT'),
(83, 2058, NULL, NULL, 0.00, NULL, '2026-01-27 12:30:30', 1, 'CATEGORY'),
(85, 2059, NULL, NULL, 0.00, NULL, '2026-01-27 12:53:28', 42, 'PRODUCT'),
(88, 2035, NULL, NULL, 0.00, NULL, '2026-01-27 13:29:00', 14, 'PRODUCT'),
(89, 2063, NULL, NULL, 0.00, NULL, '2026-01-27 13:29:30', 7, 'CATEGORY'),
(90, 2054, NULL, NULL, 0.00, NULL, '2026-01-28 04:44:37', 5, 'CATEGORY');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bundle_items`
--
ALTER TABLE `bundle_items`
  ADD PRIMARY KEY (`bundle_id`),
  ADD KEY `promotion_id` (`promotion_id`),
  ADD KEY `main_product_id` (`main_product_id`),
  ADD KEY `gift_product_id` (`gift_product_id`),
  ADD KEY `idx_bundle_product` (`product_id`);

--
-- Indexes for table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`promotion_id`),
  ADD KEY `idx_start_end_date` (`start_date`,`end_date`);

--
-- Indexes for table `promotion_targets`
--
ALTER TABLE `promotion_targets`
  ADD PRIMARY KEY (`promo_target_id`),
  ADD KEY `promotion_id` (`promotion_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `category_id` (`category_id`),
  ADD KEY `idx_target_type_id` (`target_type`,`target_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bundle_items`
--
ALTER TABLE `bundle_items`
  MODIFY `bundle_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Kh?a ch?nh, m? duy nh?t c?a lu?t t?ng/combo', AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `promotion_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Kh?a ch?nh, m? duy nh?t c?a ch??ng tr?nh khuy?n m?i', AUTO_INCREMENT=2065;

--
-- AUTO_INCREMENT for table `promotion_targets`
--
ALTER TABLE `promotion_targets`
  MODIFY `promo_target_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Kh?a ch?nh', AUTO_INCREMENT=91;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bundle_items`
--
ALTER TABLE `bundle_items`
  ADD CONSTRAINT `FKnovhg7gfyangkg1ftcjhmltw6` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`);

--
-- Constraints for table `promotion_targets`
--
ALTER TABLE `promotion_targets`
  ADD CONSTRAINT `FKadouvnh81yd8a64bk7wn435je` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
