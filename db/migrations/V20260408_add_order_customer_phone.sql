-- Migration: add customer_phone column to orders and backfill from customers
ALTER TABLE `orders`
  ADD COLUMN `customer_phone` VARCHAR(255) DEFAULT NULL;

-- Backfill existing orders that already reference a customer
UPDATE `orders` o
JOIN `customers` c ON o.customer_id = c.id
SET o.customer_phone = c.phone
WHERE o.customer_id IS NOT NULL AND (o.customer_phone IS NULL OR o.customer_phone = '');

-- (Run this on your MySQL instance to apply the schema change.)
