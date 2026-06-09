-- Migration: add user account linkage fields to customer table
ALTER TABLE `customers`
  ADD COLUMN IF NOT EXISTS `user_id` bigint DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `username` varchar(255) DEFAULT NULL;

ALTER TABLE `customers`
  ADD UNIQUE INDEX IF NOT EXISTS `uk_customers_user_id` (`user_id`),
  ADD UNIQUE INDEX IF NOT EXISTS `uk_customers_username` (`username`);
