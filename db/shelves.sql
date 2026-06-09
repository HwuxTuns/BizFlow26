-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Máy chủ: mysql:3306
-- Thời gian đã tạo: Th2 03, 2026 lúc 02:17 PM
-- Phiên bản máy phục vụ: 8.0.45
-- Phiên bản PHP: 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `bizflow_inventory_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `shelves`
--

CREATE TABLE `shelves` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL COMMENT 'ID sáº£n pháº©m trÃªn ká»‡',
  `quantity` int NOT NULL DEFAULT '0' COMMENT 'Sá»‘ lÆ°á»£ng sáº£n pháº©m trÃªn ká»‡',
  `created_at` datetime DEFAULT NULL COMMENT 'Thá»i gian táº¡o',
  `updated_at` datetime DEFAULT NULL COMMENT 'Thá»i gian cáº­p nháº­t',
  `updated_by` bigint DEFAULT NULL COMMENT 'ID ngÆ°á»i cáº­p nháº­t'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ká»‡ hÃ ng - OWNER Ä‘Æ°a tá»« kho lÃªn, nhÃ¢n viÃªn bÃ¡n tá»« ká»‡';

--
-- Đang đổ dữ liệu cho bảng `shelves`
--

INSERT INTO `shelves` (`id`, `product_id`, `quantity`, `created_at`, `updated_at`, `updated_by`) VALUES
(3, 2, 6, '2026-02-03 18:19:47', '2026-02-03 18:27:13', 3);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `shelves`
--
ALTER TABLE `shelves`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_shelf_product` (`product_id`),
  ADD KEY `idx_shelf_quantity` (`quantity`),
  ADD KEY `idx_shelf_updated` (`updated_at`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `shelves`
--
ALTER TABLE `shelves`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
