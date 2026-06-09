-- =====================================================
-- Return Requests Table - BizFlow Sales DB
-- Quản lý yêu cầu đổi trả hàng
-- =====================================================

USE bizflow_sales_db;

CREATE TABLE IF NOT EXISTS `return_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT 'Mã đơn hàng gốc',
  `invoice_number` varchar(30) DEFAULT NULL COMMENT 'Số hoá đơn',
  `customer_id` bigint DEFAULT NULL COMMENT 'Mã khách hàng',
  `customer_name` varchar(255) DEFAULT NULL,
  `customer_phone` varchar(20) DEFAULT NULL,
  `product_id` bigint NOT NULL COMMENT 'Mã sản phẩm cần đổi/trả',
  `product_name` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT 1 COMMENT 'Số lượng đổi/trả',
  `reason` varchar(50) NOT NULL COMMENT 'Loại lý do: DAMAGED, WRONG_PRODUCT, NOT_SATISFIED, DEFECTIVE, OTHER',
  `reason_detail` text DEFAULT NULL COMMENT 'Chi tiết lý do',
  `evidence_images` json DEFAULT NULL COMMENT 'Danh sách URL hình ảnh minh chứng',
  `request_type` varchar(20) NOT NULL DEFAULT 'REFUND' COMMENT 'EXCHANGE hoặc REFUND',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, COMPLETED',
  `admin_note` text DEFAULT NULL COMMENT 'Ghi chú của admin',
  `refund_amount` decimal(15,2) DEFAULT NULL COMMENT 'Số tiền hoàn (nếu REFUND)',
  `created_by` bigint DEFAULT NULL COMMENT 'ID nhân viên/khách hàng tạo yêu cầu',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_return_order_id` (`order_id`),
  KEY `idx_return_customer_id` (`customer_id`),
  KEY `idx_return_status` (`status`),
  KEY `idx_return_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
