-- Migration: ensure customer_phone exists and backfill from customer DB
ALTER TABLE `orders`
  ADD COLUMN IF NOT EXISTS `customer_phone` VARCHAR(255) DEFAULT NULL;

-- Backfill existing orders from customers table in customer DB
UPDATE `bizflow_sales_db`.`orders` o
JOIN `bizflow_customer_db`.`customers` c ON o.customer_id = c.id
SET o.customer_phone = c.phone
WHERE o.customer_id IS NOT NULL AND (o.customer_phone IS NULL OR o.customer_phone = '');

-- (Run this on your MySQL instance to apply the schema change.)
