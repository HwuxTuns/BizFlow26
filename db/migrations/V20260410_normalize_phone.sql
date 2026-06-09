-- Migration: normalize phone numbers in customers and orders (digits-only)
-- Run on MySQL 8+ (uses REGEXP_REPLACE)

-- 1) Normalize customer phones to digits-only
UPDATE `bizflow_customer_db`.`customers`
SET phone = REGEXP_REPLACE(phone, '[^0-9]', '')
WHERE phone IS NOT NULL AND phone <> '';

-- 2) Normalize existing orders.customer_phone to digits-only
UPDATE `bizflow_sales_db`.`orders`
SET customer_phone = REGEXP_REPLACE(customer_phone, '[^0-9]', '')
WHERE customer_phone IS NOT NULL AND customer_phone <> '';

-- 3) Backfill orders.customer_phone from customers when missing
UPDATE `bizflow_sales_db`.`orders` o
JOIN `bizflow_customer_db`.`customers` c ON o.customer_id = c.id
SET o.customer_phone = c.phone
WHERE o.customer_id IS NOT NULL AND (o.customer_phone IS NULL OR o.customer_phone = '');

-- 4) Add simple indexes for faster phone lookups (safe if indexes not present)
-- Note: If your MySQL version does not support IF NOT EXISTS for indexes, run these separately and skip if index exists.
ALTER TABLE `bizflow_customer_db`.`customers` ADD INDEX `idx_customers_phone` (`phone`(20));
ALTER TABLE `bizflow_sales_db`.`orders` ADD INDEX `idx_orders_customer_phone` (`customer_phone`(20));

-- End of migration
