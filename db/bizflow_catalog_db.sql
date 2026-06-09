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
-- Database: `bizflow_catalog_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` bigint NOT NULL,
  `category_name` varchar(255) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `category_name`, `parent_id`, `status`, `created_at`, `description`, `updated_at`) VALUES
(1, 'Nước Giải Khát', NULL, 'active', NULL, NULL, NULL),
(2, 'Đồ Ăn Vặt', NULL, 'active', NULL, NULL, NULL),
(3, 'Hóa Mỹ Phẩm', NULL, 'active', NULL, NULL, NULL),
(4, 'Gia Vị & Nước Chấm', NULL, 'active', NULL, NULL, NULL),
(5, 'Sản Phẩm Chăm Sóc Nhà Cửa', NULL, 'active', NULL, NULL, NULL),
(6, 'Bánh Kẹo', NULL, 'active', NULL, NULL, NULL),
(7, 'Bia & Rượu', NULL, 'active', NULL, NULL, NULL),
(8, 'Mì, Phở, Cháo Gói', NULL, 'active', NULL, NULL, NULL),
(9, 'Đồ Hộp & Thực Phẩm Đóng Hộp', NULL, 'active', NULL, NULL, NULL),
(10, 'Thuốc Lá & Diêm', NULL, 'active', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` bigint NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `sku` varchar(100) NOT NULL,
  `barcode` varchar(100) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `unit` varchar(50) NOT NULL,
  `price` double DEFAULT NULL,
  `cost_price` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `description` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `stock` int DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `product_name`, `sku`, `barcode`, `category_id`, `unit`, `price`, `cost_price`, `status`, `description`, `created_at`, `updated_at`, `active`, `code`, `name`, `stock`, `category`) VALUES
(1, 'Coca-Cola lon 330ml', 'CC330', '8934567000010', 1, 'lon', 10000, 7500, 'active', 'Nước ngọt có ga', '2025-12-29 17:04:24', NULL, NULL, 'CC330', 'Coca-Cola lon 330ml', 20, NULL),
(2, 'Trà Xanh Không Độ chai 500ml', 'TXKD500', '8934567000027', 1, 'chai', 12000, 9000, 'active', 'Trà giải khát không đường', '2025-12-29 17:04:24', NULL, NULL, '', '', 120, NULL),
(3, 'Nước suối Aquafina 500ml', 'AQF500', '8934567000034', 1, 'chai', 5000, 3500, 'active', 'Nước tinh khiết', '2025-12-29 17:04:24', NULL, NULL, '', '', 120, NULL),
(4, 'Bia Sài Gòn Lager 330ml', 'SGL330', '8934567000041', 1, 'lon', 15000, 11000, 'active', 'Bia Lager (có thể dùng chung ID 7 nếu không muốn tách)', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(5, 'Pepsi lon 330ml', 'PS330', '8934567000058', 1, 'lon', 10000, 7500, 'active', 'Nước ngọt có ga vị chanh', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(6, 'Snack Oishi vị Phô Mai 35g', 'OIS-PM35', '8934567000065', 2, 'gói', 8000, 5500, 'active', 'Bánh snack khoai tây', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(7, 'Khô Gà Lá Chanh 100g', 'KG-LC100', '8934567000072', 2, 'túi', 35000, 25000, 'active', 'Thực phẩm ăn liền', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(8, 'Hạt Hướng Dương Vị Muối 250g', 'HD-MS250', '8934567000089', 2, 'gói', 20000, 14000, 'active', 'Hạt rang ăn liền', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(9, 'Bánh Quy Cosy dừa 160g', 'CQ-DY160', '8934567000096', 2, 'gói', 18000, 12500, 'active', 'Bánh quy ngọt', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(10, 'Kẹo Alpenliebe vị caramen', 'ALP-CR', '8934567000102', 2, 'gói', 15000, 10000, 'active', 'Kẹo cứng (có thể dùng chung ID 6)', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(11, 'Dầu Gội Sunsilk Mềm Mượt 320g', 'DG-SM320', '8934567000119', 3, 'chai', 65000, 45000, 'active', 'Dầu gội đầu', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(12, 'Kem Đánh Răng P/S Bảo Vệ 120g', 'KDR-PSBV', '8934567000126', 3, 'tuýp', 30000, 20000, 'active', 'Kem đánh răng cơ bản', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(13, 'Xà Bông Lifebuoy diệt khuẩn 90g', 'XB-LB90', '8934567000133', 3, 'bánh', 15000, 10000, 'active', 'Xà bông tắm/rửa tay', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(14, 'Dao cạo râu Gillette 1 lưỡi', 'DCR-GL1', '8934567000140', 3, 'cái', 5000, 3000, 'active', 'Dụng cụ cá nhân', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(15, 'Khăn Giấy Gấu Trúc 180 tờ', 'KG-GT180', '8934567000157', 3, 'gói', 12000, 8000, 'active', 'Khăn giấy khô', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(16, 'Nước Mắm Chin-su 500ml', 'NM-CS500', '8934567000164', 4, 'chai', 45000, 30000, 'active', 'Nước mắm cá cơm', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(17, 'Dầu Ăn Tường An 1 Lít', 'DA-TA1L', '8934567000171', 4, 'chai', 60000, 40000, 'active', 'Dầu thực vật', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(18, 'Muối I-ốt Bạc Liêu 500g', 'M-BL500', '8934567000188', 4, 'gói', 5000, 3000, 'active', 'Muối ăn thông thường', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(19, 'Đường Trắng Biên Hòa 1kg', 'DT-BH1KG', '8934567000195', 4, 'gói', 25000, 18000, 'active', 'Đường tinh luyện', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(20, 'Bột Ngọt Ajinomoto 450g', 'BN-AJI450', '8934567000201', 4, 'gói', 35000, 25000, 'active', 'Chất điều vị', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(21, 'Nước Rửa Chén Mỹ Hảo 800g', 'NRC-MH800', '8934567000218', 5, 'chai', 25000, 15000, 'active', 'Nước rửa chén bát', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(22, 'Bột Giặt OMO Matic 800g', 'BG-OMO800', '8934567000225', 5, 'túi', 45000, 30000, 'active', 'Bột giặt máy giặt', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(23, 'Nước Lau Sàn Gift 1 Lít', 'NLS-GF1L', '8934567000232', 5, 'chai', 35000, 22000, 'active', 'Chất tẩy rửa sàn nhà', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(24, 'Thuốc diệt muỗi Mosfly chai', 'TDM-MOS', '8934567000249', 5, 'chai', 70000, 50000, 'active', 'Chất diệt côn trùng', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(25, 'Túi đựng rác tự phân hủy (3 cuộn)', 'TDR-3C', '8934567000256', 5, 'gói', 15000, 10000, 'active', 'Dụng cụ vệ sinh', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(26, 'Bánh Chocopie Hộp 12 cái', 'CP-12C', '8934567000263', 6, 'hộp', 40000, 28000, 'active', 'Bánh xốp phủ sô cô la', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(27, 'Kẹo Sugus Trái Cây 150g', 'K-SUG150', '8934567000270', 6, 'túi', 20000, 13000, 'active', 'Kẹo dẻo mềm', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(28, 'Bánh Mì Sữa Tươi Kinh Đô', 'BM-KDS', '8934567000287', 6, 'gói', 12000, 8000, 'active', 'Bánh mì ngọt ăn sáng', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(29, 'Thạch Rau Câu Long Hải 400g', 'TRC-LH400', '8934567000294', 6, 'túi', 15000, 10000, 'active', 'Tráng miệng lạnh', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(30, 'Kẹo Socola Kitkat thanh', 'SC-KKTH', '8934567000300', 6, 'thanh', 8000, 5000, 'active', 'Bánh xốp phủ socola', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(31, 'Bia Tiger lon 330ml', 'BT-330', '8934567000317', 7, 'lon', 17000, 12500, 'active', 'Bia Lager cao cấp', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(32, 'Bia Heineken lon 330ml', 'BH-330', '8934567000324', 7, 'lon', 20000, 15000, 'active', 'Bia nhập khẩu', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(33, 'Rượu Vodka Hà Nội 700ml', 'R-VDHN', '8934567000331', 7, 'chai', 150000, 100000, 'active', 'Rượu mạnh', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(34, 'Bia 333 lon 330ml', 'B3-330', '8934567000348', 7, 'lon', 14000, 10000, 'active', 'Bia truyền thống', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(35, 'Bia Larue lon 330ml', 'BL-330', '8934567000355', 7, 'lon', 13000, 9000, 'active', 'Bia phổ thông', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(36, 'Mì Hảo Hảo Tôm Chua Cay', 'MHHTC', '8934567000362', 8, 'gói', 6000, 4000, 'active', 'Mì ăn liền phổ thông', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(37, 'Phở Bò Gói Vifon', 'PB-VF', '8934567000379', 8, 'gói', 12000, 8000, 'active', 'Phở ăn liền', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(38, 'Mì Omachi Xốt Thịt Heo', 'M-OMXTH', '8934567000386', 8, 'gói', 10000, 6500, 'active', 'Mì không chiên', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(39, 'Cháo Thịt Bằm gói 50g', 'CB-50G', '8934567000393', 8, 'gói', 8000, 5000, 'active', 'Cháo ăn liền dinh dưỡng', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(40, 'Mì Kokomi Đại gói', 'MKD-GOI', '8934567000409', 8, 'gói', 5500, 3500, 'active', 'Mì gói lớn', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(41, 'Pate Cột Đèn Hải Phòng 100g', 'PCĐ-100', '8934567000416', 9, 'hộp', 25000, 18000, 'active', 'Thịt xay đóng hộp', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(42, 'Cá Hộp Sốt Cà Chua 3 Cô Gái', 'CH-3CG', '8934567000423', 9, 'hộp', 20000, 14000, 'active', 'Cá hộp ăn liền', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(43, 'Thịt Heo 2 Lát Đóng Hộp', 'TH2L-DH', '8934567000430', 9, 'hộp', 40000, 28000, 'active', 'Thịt heo chế biến sẵn', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(44, 'Sữa Đặc Ông Thọ Vàng 380g', 'SD-OTV', '8934567000447', 9, 'lon', 28000, 20000, 'active', 'Sữa đặc có đường', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(45, 'Dưa Chuột Muối chua 500g', 'DCM-500', '8934567000454', 9, 'hũ', 30000, 21000, 'active', 'Rau củ muối chua', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(46, 'Thuốc Lá Vinataba Gói', 'TL-VNTB', '8934567000461', 10, 'gói', 30000, 22000, 'active', 'Thuốc lá thông dụng', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(47, 'Thuốc Lá 555 Gói', 'TL-555', '8934567000478', 10, 'gói', 35000, 26000, 'active', 'Thuốc lá cao cấp hơn', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(48, 'Bật Lửa Gas BIC (màu xanh)', 'BL-BICX', '8934567000485', 10, 'cái', 8000, 5000, 'active', 'Bật lửa dùng 1 lần', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(49, 'Diêm Thống Nhất Hộp Nhỏ', 'D-TN', '8934567000492', 10, 'hộp', 2000, 1000, 'active', 'Diêm quẹt', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(50, 'Thuốc Lá Kent Gói', 'TL-KNT', '8934567000508', 10, 'gói', 40000, 30000, 'active', 'Thuốc lá nhập khẩu', '2025-12-29 17:04:24', NULL, NULL, '', '', 20, NULL),
(51, 'Nước tăng lực Red Bull 250ml', 'NTL-RB', '8934567000515', 1, 'lon', 18000, 13000, 'active', 'Nước uống tăng lực Thái Lan', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(52, 'Sữa tươi Vinamilk không đường 1L', 'ST-VNM-KS', '8934567000522', 1, 'hộp', 35000, 25000, 'active', 'Sữa tươi tiệt trùng 100%', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(53, 'Nước ép cam Vfresh 1L', 'NE-VFC', '8934567000539', 1, 'hộp', 40000, 28000, 'active', 'Nước ép trái cây nguyên chất', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(54, 'Nước suối Lavie 1.5L', 'NUC-LV15', '8934567000546', 1, 'chai', 8000, 5000, 'active', 'Nước khoáng thiên nhiên', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(55, 'Nước yến ngân nhĩ 240ml', 'NY-NN240', '8934567000553', 1, 'lon', 25000, 18000, 'active', 'Nước giải khát bổ dưỡng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(56, 'Bia 333 thùng 24 lon', 'B333-T24', '8934567000560', 1, 'thùng', 320000, 250000, 'active', 'Bia thùng 24 lon', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(57, 'Trà đào Cozy hộp 25 gói', 'TD-CZY25', '8934567000577', 1, 'hộp', 45000, 32000, 'active', 'Trà hòa tan vị đào', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(58, 'Sting dâu lon 330ml', 'NTL-STING', '8934567000584', 1, 'lon', 12000, 8500, 'active', 'Nước tăng lực vị dâu', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(59, 'Sữa chua uống Probi 65ml (lốc 5)', 'SCU-PB', '8934567000591', 1, 'lốc', 22000, 16000, 'active', 'Sữa chua uống lợi khuẩn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(60, 'Nước ép dứa Dole 330ml', 'NED-DL330', '8934567000607', 1, 'lon', 15000, 10000, 'active', 'Nước ép dứa đóng lon', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(61, 'Bánh gạo One.One vị bò 100g', 'BG-OOVB', '8934567000614', 2, 'gói', 15000, 10000, 'active', 'Bánh gạo giòn tan', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(62, 'Snack Poca vị muối ớt 70g', 'SK-POCA', '8934567000621', 2, 'gói', 18000, 12000, 'active', 'Snack khoai tây lát muối ớt', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(63, 'Rong Biển Ăn Liền Taekyung 5g', 'RB-TK', '8934567000638', 2, 'gói', 7000, 4500, 'active', 'Rong biển sấy khô Hàn Quốc', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(64, 'Hạt điều rang muối 500g', 'HD-RM500', '8934567000645', 2, 'hộp', 150000, 110000, 'active', 'Hạt điều Bình Phước', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(65, 'Thịt bò khô miếng 50g', 'TBK-M50', '8934567000652', 2, 'gói', 45000, 32000, 'active', 'Thịt bò sấy khô xé sợi', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(66, 'Bánh phồng tôm Sa Giang 200g', 'BPT-SG200', '8934567000669', 2, 'gói', 30000, 20000, 'active', 'Bánh phồng tôm chưa chiên', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(67, 'Socola thanh Mars 50g', 'SC-MAR50', '8934567000676', 2, 'thanh', 18000, 12000, 'active', 'Socola nhân caramen', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(68, 'Bim Bim Lay\'s vị tự nhiên 50g', 'BB-LAYS', '8934567000683', 2, 'gói', 10000, 6500, 'active', 'Snack khoai tây lát mỏng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(69, 'Kẹo dẻo Chuppa Chups 75g', 'KD-CC75', '8934567000690', 2, 'gói', 15000, 10000, 'active', 'Kẹo dẻo hình thù', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(70, 'Mứt gừng sấy dẻo 200g', 'MG-SD200', '8934567000706', 2, 'hộp', 50000, 35000, 'active', 'Mứt gừng đặc sản', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(71, 'Sữa tắm Enchanteur 450ml', 'ST-ECH', '8934567000713', 3, 'chai', 85000, 60000, 'active', 'Sữa tắm hương nước hoa', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(72, 'Dầu xả TRESemmé 340g', 'DX-TRE', '8934567000720', 3, 'chai', 70000, 50000, 'active', 'Dầu xả dưỡng tóc óng mượt', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(73, 'Kem chống nắng Bioré 50ml', 'KCN-BIO', '8934567000737', 3, 'tuýp', 90000, 65000, 'active', 'Bảo vệ da SPF50+', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(74, 'Son dưỡng môi Vaseline 10g', 'SDM-VAS', '8934567000744', 3, 'hộp', 35000, 25000, 'active', 'Dưỡng ẩm môi khô nẻ', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(75, 'Băng vệ sinh Diana Sensi 8 miếng', 'BVS-DS8', '8934567000751', 3, 'gói', 25000, 18000, 'active', 'Băng vệ sinh hàng ngày', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(76, 'Nước tẩy trang L\'Oréal 400ml', 'NTT-LO400', '8934567000768', 3, 'chai', 130000, 90000, 'active', 'Làm sạch sâu da mặt', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(77, 'Mặt nạ giấy dưỡng ẩm (gói)', 'MN-GA', '8934567000775', 3, 'gói', 15000, 10000, 'active', 'Mặt nạ cấp ẩm', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(78, 'Sữa rửa mặt Cetaphil 125ml', 'SRM-CET', '8934567000782', 3, 'chai', 80000, 55000, 'active', 'Sữa rửa mặt dịu nhẹ', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(79, 'Khăn ướt Bobby không mùi (100 tờ)', 'KU-BBY', '8934567000799', 3, 'gói', 30000, 20000, 'active', 'Khăn ướt vệ sinh cá nhân', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(80, 'Kem dưỡng ẩm Nivea Soft 50ml', 'KDA-NS50', '8934567000805', 3, 'hộp', 45000, 30000, 'active', 'Kem dưỡng ẩm toàn thân', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(81, 'Tương ớt Chin-su chai 250g', 'TO-CS250', '8934567000812', 4, 'chai', 18000, 12000, 'active', 'Tương ớt cay nồng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(82, 'Xì Dầu Maggi chai 200ml', 'XD-MAG200', '8934567000829', 4, 'chai', 22000, 15000, 'active', 'Nước tương đậu nành', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(83, 'Giấm gạo Ajinomoto 400ml', 'G-AJI400', '8934567000836', 4, 'chai', 15000, 10000, 'active', 'Giấm dùng nấu ăn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(84, 'Hạt nêm Knorr 400g', 'HN-KNR400', '8934567000843', 4, 'gói', 30000, 21000, 'active', 'Gia vị nêm nếm thịt thăn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(85, 'Tiêu xay Vifon 50g', 'TX-VF50', '8934567000850', 4, 'hộp', 25000, 17000, 'active', 'Bột tiêu xay nguyên chất', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(86, 'Dầu hào Maggi 350g', 'DH-MAG350', '8934567000867', 4, 'chai', 35000, 24000, 'active', 'Dầu hào nấm hương', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(87, 'Bột canh I-ốt 190g', 'BC-I-190', '8934567000874', 4, 'gói', 8000, 5000, 'active', 'Bột canh nêm nếm', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(88, 'Nước tương Phú Sĩ 500ml', 'NT-PS500', '8934567000881', 4, 'chai', 18000, 12000, 'active', 'Nước tương phổ thông', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(89, 'Dầu mè đen 200ml', 'DM-200', '8934567000898', 4, 'chai', 30000, 21000, 'active', 'Dầu mè thơm', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(90, 'Bột mì đa dụng 500g', 'BM-500', '8934567000904', 4, 'gói', 15000, 10000, 'active', 'Bột dùng làm bánh', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(91, 'Nước rửa tay Lifebuoy 450g', 'NRT-LB450', '8934567000911', 5, 'chai', 55000, 38000, 'active', 'Nước rửa tay diệt khuẩn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(92, 'Nước xả vải Comfort 800ml', 'NXV-CF800', '8934567000928', 5, 'túi', 40000, 28000, 'active', 'Làm mềm và thơm quần áo', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(93, 'Giấy vệ sinh Sài Gòn 10 cuộn', 'GVS-SG10', '8934567000935', 5, 'gói', 50000, 35000, 'active', 'Giấy vệ sinh 3 lớp', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(94, 'Dầu hỏa thắp sáng 500ml', 'DH-500', '8934567000942', 5, 'chai', 15000, 10000, 'active', 'Chất đốt thắp sáng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(95, 'Nến thơm Lavender', 'NT-LV', '8934567000959', 5, 'cái', 35000, 24000, 'active', 'Khử mùi, tạo hương thơm', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(96, 'Nước tẩy bồn cầu Vim 450ml', 'NTBC-VM', '8934567000966', 5, 'chai', 30000, 20000, 'active', 'Chất tẩy rửa nhà vệ sinh', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(97, 'Túi đựng rác 5kg (3 cuộn)', 'TDR-5KG', '8934567000973', 5, 'gói', 25000, 17000, 'active', 'Túi rác tự phân hủy', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(98, 'Bột thông cống 100g', 'BTC-100', '8934567000980', 5, 'gói', 15000, 10000, 'active', 'Hóa chất thông cống', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(99, 'Khăn lau đa năng 3M Scotch-Brite', 'KL-3MSB', '8934567000997', 5, 'cái', 40000, 28000, 'active', 'Khăn lau thấm nước', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(100, 'Nước lau kính Cif 500ml', 'NLK-CIF', '8934567001000', 5, 'chai', 35000, 24000, 'active', 'Làm sạch gương kính', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(101, 'Bánh quy bơ Danisa 454g', 'BQB-DAN', '8934567001017', 6, 'hộp', 90000, 65000, 'active', 'Bánh quy bơ hộp thiếc', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(102, 'Bánh Custas kem trứng hộp 6 cái', 'BC-KEM6', '8934567001024', 6, 'hộp', 35000, 24000, 'active', 'Bánh mềm nhân kem trứng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(103, 'Kẹo cao su Doublemint', 'KCS-DM', '8934567001031', 6, 'gói', 10000, 6000, 'active', 'Kẹo cao su bạc hà', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(104, 'Bánh Goute mè giòn 144g', 'B-GME', '8934567001048', 6, 'gói', 25000, 17000, 'active', 'Bánh mặn giòn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(105, 'Socola sữa Milo Cube', 'SC-MLC', '8934567001055', 6, 'gói', 50000, 35000, 'active', 'Socola viên ăn vặt', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(106, 'Bánh AFC lúa mì 200g', 'B-AFC', '8934567001062', 6, 'gói', 28000, 20000, 'active', 'Bánh quy ăn kiêng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(107, 'Kẹo dẻo Haribo Gummy Bear', 'KD-HGB', '8934567001079', 6, 'gói', 30000, 21000, 'active', 'Kẹo dẻo hình gấu', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(108, 'Bánh mì sandwich lát (tươi)', 'BMS-LAT', '8934567001086', 6, 'gói', 18000, 12000, 'active', 'Bánh mì tươi ăn sáng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(109, 'Thạch rau câu Zai Zai (hộp)', 'TRC-ZZ', '8934567001093', 6, 'hộp', 40000, 28000, 'active', 'Thạch rau câu nhiều vị', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(110, 'Bánh quy Oreo nhân kem 133g', 'BQ-OREO', '8934567001109', 6, 'gói', 15000, 10000, 'active', 'Bánh quy sô cô la', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(111, 'Rượu vang Đà Lạt Classic 750ml', 'RV-DL750', '8934567001116', 7, 'chai', 120000, 85000, 'active', 'Rượu vang đỏ phổ thông', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(112, 'Bia Tiger Crystal chai 330ml (thùng 24)', 'BTC-24', '8934567001123', 7, 'thùng', 400000, 300000, 'active', 'Bia Crystal nhẹ', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(113, 'Rượu Soju Chum Churum 360ml', 'RS-CC', '8934567001130', 7, 'chai', 65000, 45000, 'active', 'Rượu Soju Hàn Quốc', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(114, 'Bia Huda lon 330ml', 'B-HDA', '8934567001147', 7, 'lon', 13000, 9500, 'active', 'Bia địa phương miền Trung', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(115, 'Nước ngọt có gas 7Up lon 330ml', 'NG-7UP', '8934567001154', 7, 'lon', 10000, 7500, 'active', 'Nước ngọt chanh', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(116, 'Bia Larue chai 450ml', 'BL-450', '8934567001161', 7, 'chai', 18000, 13000, 'active', 'Chai bia truyền thống', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(117, 'Rượu Nếp Mới 500ml', 'RNM-500', '8934567001178', 7, 'chai', 50000, 35000, 'active', 'Rượu nếp truyền thống', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(118, 'Bia Strongbow Vị Dâu lon', 'BS-DV', '8934567001185', 7, 'lon', 20000, 15000, 'active', 'Cider trái cây', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(119, 'Bia Sapporo lon 330ml', 'BS-330', '8934567001192', 7, 'lon', 19000, 14000, 'active', 'Bia Nhật Bản', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(120, 'Rượu Whisky Johnnie Walker Red Label 750ml', 'RW-JWRL', '8934567001208', 7, 'chai', 550000, 400000, 'active', 'Rượu mạnh', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(121, 'Mì Tiến Vua Bò Hầm', 'M-TVBH', '8934567001215', 8, 'gói', 7000, 4500, 'active', 'Mì không chiên vị bò', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(122, 'Hủ tiếu Nam Vang Gói', 'HT-NVG', '8934567001222', 8, 'gói', 15000, 10000, 'active', 'Hủ tiếu ăn liền', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(123, 'Cháo Yến Mạch ăn liền 50g', 'CYM-50', '8934567001239', 8, 'gói', 9000, 6000, 'active', 'Cháo yến mạch ăn liền', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(124, 'Mì 3 Miền Tôm Chua Cay', 'M3M-TCC', '8934567001246', 8, 'gói', 5500, 3500, 'active', 'Mì phổ thông vị tôm chua cay', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(125, 'Miến Phú Hương Sườn Heo', 'MPH-SH', '8934567001253', 8, 'gói', 10000, 7000, 'active', 'Miến ăn liền vị sườn heo', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(126, 'Mì lẩu thái Gấu Đỏ', 'MLT-GD', '8934567001260', 8, 'gói', 6500, 4000, 'active', 'Mì gói vị lẩu thái', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(127, 'Bún riêu cua ăn liền', 'BRC-AL', '8934567001277', 8, 'gói', 13000, 9000, 'active', 'Bún ăn liền vị riêu cua', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(128, 'Cháo đậu xanh gói', 'CDX-GOI', '8934567001284', 8, 'gói', 7000, 4500, 'active', 'Cháo dinh dưỡng đậu xanh', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(129, 'Mì Reeva Lẩu nấm', 'M-RVLN', '8934567001291', 8, 'gói', 8000, 5500, 'active', 'Mì có sợi khoai tây', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(130, 'Phở khô Sông Hậu 400g', 'PK-SH400', '8934567001307', 8, 'gói', 20000, 14000, 'active', 'Phở khô làm bún', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(131, 'Thịt lợn hầm Tiên Yết 150g', 'TLH-TY', '8934567001314', 9, 'hộp', 35000, 25000, 'active', 'Thịt lợn hầm sẵn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(132, 'Cá Ngừ Ngâm Dầu hộp 185g', 'CN-ND', '8934567001321', 9, 'hộp', 60000, 42000, 'active', 'Cá ngừ đóng hộp', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(133, 'Đậu cô ve muối chua 400g', 'DCVM-400', '8934567001338', 9, 'hũ', 25000, 17000, 'active', 'Rau củ ngâm chua', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(134, 'Sữa Đặc Ngôi Sao Phương Nam 380g', 'SD-NSPN', '8934567001345', 9, 'lon', 26000, 19000, 'active', 'Sữa đặc có đường', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(135, 'Lạp xưởng đóng gói 500g', 'LX-DG500', '8934567001352', 9, 'gói', 90000, 65000, 'active', 'Thực phẩm chế biến', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(136, 'Nước cốt gà Vifon 180g', 'NCG-VF', '8934567001369', 9, 'hộp', 45000, 30000, 'active', 'Nước cốt hầm xương', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(137, 'Thịt gà xé phay đóng hộp 150g', 'TGXP-150', '8934567001376', 9, 'hộp', 38000, 26000, 'active', 'Thịt gà đóng hộp ăn liền', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(138, 'Bò kho đóng hộp 300g', 'BK-DH300', '8934567001383', 9, 'hộp', 55000, 40000, 'active', 'Bò kho chế biến sẵn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(139, 'Dứa (thơm) đóng hộp cắt lát 565g', 'DT-CLL', '8934567001390', 9, 'lon', 40000, 28000, 'active', 'Trái cây đóng hộp', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(140, 'Đậu phụ chiên đóng hộp 200g', 'DPC-DH', '8934567001406', 9, 'hộp', 20000, 13000, 'active', 'Đậu phụ chiên sẵn', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(141, 'Thuốc Lá Craven A Gói', 'TL-CA', '8934567001413', 10, 'gói', 32000, 24000, 'active', 'Thuốc lá', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(142, 'Thuốc Lá Marlboro Lights Gói', 'TL-MLT', '8934567001420', 10, 'gói', 45000, 33000, 'active', 'Thuốc lá nhẹ', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(143, 'Bật Lửa Đá Zippo (chưa đổ xăng)', 'BL-ZPO', '8934567001437', 10, 'cái', 350000, 250000, 'active', 'Bật lửa tái sử dụng', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(144, 'Xăng Bật Lửa Zippo 125ml', 'XBL-ZPO', '8934567001444', 10, 'chai', 50000, 35000, 'active', 'Nhiên liệu bật lửa', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(145, 'Giấy cuốn thuốc lá 100 tờ', 'GCT-100', '8934567001451', 10, 'tệp', 15000, 10000, 'active', 'Giấy cuốn thuốc', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(146, 'Tẩu hút thuốc lá nhỏ', 'THTL-N', '8934567001468', 10, 'cái', 80000, 55000, 'active', 'Dụng cụ hút thuốc', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(147, 'Thuốc Lá Thăng Long', 'TL-TL', '8934567001475', 10, 'gói', 25000, 18000, 'active', 'Thuốc lá phổ thông Việt Nam', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(148, 'Diêm hộp lớn 100 que', 'D-HL', '8934567001482', 10, 'hộp', 3000, 1500, 'active', 'Diêm dùng nhiều lần', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(149, 'Bật lửa điện sạc USB', 'BL-USB', '8934567001499', 10, 'cái', 120000, 80000, 'active', 'Bật lửa hiện đại', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(150, 'Giấy quấn thuốc lá Zig-Zag', 'GQTL-ZZ', '8934567001505', 10, 'gói', 20000, 13000, 'active', 'Giấy cuốn thuốc lá', '2025-12-29 17:08:00', NULL, NULL, '', '', 20, NULL),
(151, 'bánh quế socola GO CHOCO', 'SP37593', '3878270544027', 4, 'hộp', 30000, 25000, 'active', NULL, '2026-01-22 16:07:32', NULL, NULL, 'SP37593', 'bánh quế socola GO CHOCO', 10, NULL),
(152, 'Kem đánh răng crest', 'SP84990', '1794132717423', 2, 'Típ', 90000, 80000, 'active', NULL, '2026-01-22 16:16:12', NULL, NULL, 'SP84990', 'Kem đánh răng crest', 10, NULL),
(153, 'Bút bi Thiên Long', 'SP62545', '1227045324841', 4, 'Cây', 3000, 1500, 'active', NULL, '2026-01-22 16:34:52', NULL, NULL, 'SP62545', 'Bút bi Thiên Long', 10, NULL),
(154, 'Bcs', 'SP44611', '7300337854582', 5, 'hop', 70000, 50000, 'active', NULL, '2026-01-22 19:47:30', NULL, NULL, 'SP44611', 'Bcs', 1, NULL),
(155, 'Thùng 48 hộp sữa tươi tiệt trùng rất ít đường Vinamilk Green Farm 180ml', 'SP34850', '5029017434238', 1, 'thùng', 420000, 390000, 'active', NULL, '2026-01-25 16:04:58', NULL, NULL, 'SP34850', 'Thùng 48 hộp sữa tươi tiệt trùng rất ít đường Vinamilk Green Farm 180ml', 150, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `product_costs`
--

CREATE TABLE `product_costs` (
  `id` bigint NOT NULL,
  `cost_price` decimal(15,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `product_costs`
--

INSERT INTO `product_costs` (`id`, `cost_price`, `created_at`, `product_id`, `updated_at`) VALUES
(1, 25000.00, '2026-01-22 23:07:32.445522', 151, '2026-01-22 23:07:32.445541'),
(2, 80000.00, '2026-01-22 23:16:12.230848', 152, '2026-01-22 23:16:12.230862'),
(3, 1500.00, '2026-01-22 23:34:52.331324', 153, '2026-01-22 23:34:52.331330'),
(4, 50000.00, '2026-01-23 02:47:30.603900', 154, '2026-01-23 02:47:30.603909'),
(5, 9000.00, '2026-01-23 14:47:28.255344', 2, '2026-01-23 14:47:28.255370'),
(6, 390000.00, '2026-01-25 23:04:58.656164', 155, '2026-01-25 23:04:58.656174'),
(7, 3500.00, '2026-01-25 23:09:46.561217', 3, '2026-01-28 14:36:45.071436'),
(8, 14000.00, '2026-01-28 14:30:39.254898', 8, '2026-01-28 14:30:39.254911');

-- --------------------------------------------------------

--
-- Table structure for table `product_cost_histories`
--

CREATE TABLE `product_cost_histories` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `cost_price` decimal(15,2) NOT NULL,
  `quantity` int NOT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `product_cost_histories`
--

INSERT INTO `product_cost_histories` (`id`, `product_id`, `cost_price`, `quantity`, `note`, `created_by`, `created_at`) VALUES
(1, 151, 25000.00, 10, NULL, NULL, '2026-01-22 23:07:32.479610'),
(2, 152, 80000.00, 10, NULL, NULL, '2026-01-22 23:16:12.260094'),
(3, 153, 1500.00, 10, NULL, NULL, '2026-01-22 23:34:52.349602'),
(4, 154, 50000.00, 1, NULL, NULL, '2026-01-23 02:47:30.618015'),
(5, 2, 9000.00, 100, ' nhập ngày  23/01/2026\r\n', NULL, '2026-01-23 14:47:28.306615'),
(6, 155, 390000.00, 150, NULL, NULL, '2026-01-25 23:04:58.679371'),
(7, 3, 3500.00, 100, '', NULL, '2026-01-25 23:09:46.568152'),
(8, 8, 14000.00, 20, '', NULL, '2026-01-28 14:30:39.528990'),
(9, 3, 3500.00, 100, '', NULL, '2026-01-28 14:36:45.115621');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD KEY `fk_category_parent` (`parent_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD UNIQUE KEY `sku` (`sku`),
  ADD KEY `fk_product_category` (`category_id`);

--
-- Indexes for table `product_costs`
--
ALTER TABLE `product_costs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_emj5ou3kymldu462sbu36chhd` (`product_id`);

--
-- Indexes for table `product_cost_histories`
--
ALTER TABLE `product_cost_histories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKagbnau0mqw831wgvk2h3v696m` (`product_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=156;

--
-- AUTO_INCREMENT for table `product_costs`
--
ALTER TABLE `product_costs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `product_cost_histories`
--
ALTER TABLE `product_cost_histories`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `product_cost_histories`
--
ALTER TABLE `product_cost_histories`
  ADD CONSTRAINT `FKagbnau0mqw831wgvk2h3v696m` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
