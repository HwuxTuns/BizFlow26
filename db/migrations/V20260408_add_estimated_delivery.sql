-- Migration: add estimated delivery window to orders
ALTER TABLE `orders`
  ADD COLUMN `estimated_delivery_from` DATETIME DEFAULT NULL,
  ADD COLUMN `estimated_delivery_to` DATETIME DEFAULT NULL;

-- (Run this on your MySQL instance to apply the schema change.)
