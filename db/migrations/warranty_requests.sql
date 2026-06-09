-- =============================================
-- Bảng: warranty_requests
-- Mô tả: Lưu trữ yêu cầu bảo hành sản phẩm
-- =============================================

CREATE TABLE IF NOT EXISTS warranty_requests (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL COMMENT 'Mã đơn hàng gốc',
    invoice_number  VARCHAR(30)     NULL COMMENT 'Mã hóa đơn',
    product_id      BIGINT          NOT NULL COMMENT 'Mã sản phẩm cần bảo hành',
    product_name    VARCHAR(255)    NULL COMMENT 'Tên sản phẩm (snapshot)',
    customer_id     BIGINT          NULL COMMENT 'Mã khách hàng',
    customer_name   VARCHAR(100)    NULL COMMENT 'Tên khách hàng (snapshot)',
    customer_phone  VARCHAR(20)     NULL COMMENT 'SĐT khách hàng',
    description     TEXT            NOT NULL COMMENT 'Mô tả lỗi / vấn đề',
    evidence_images JSON            NULL COMMENT 'Danh sách ảnh minh chứng (Base64 hoặc URL)',
    warranty_days   INT             NOT NULL DEFAULT 30 COMMENT 'Số ngày bảo hành của sản phẩm',
    purchase_date   DATETIME        NULL COMMENT 'Ngày mua hàng (từ đơn gốc)',
    warranty_expiry DATETIME        NULL COMMENT 'Ngày hết hạn bảo hành',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, REPAIRING, COMPLETED',
    admin_note      TEXT            NULL COMMENT 'Ghi chú xử lý của admin',
    created_by      BIGINT          NULL COMMENT 'ID người tạo yêu cầu',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_warranty_order (order_id),
    INDEX idx_warranty_customer (customer_id),
    INDEX idx_warranty_status (status),
    INDEX idx_warranty_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
