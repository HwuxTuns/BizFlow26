-- Migration: ensure customer_phone exists and backfill from customer DB (robust for older MySQL)
-- Add column only if missing using information_schema
SET @schema_name = 'bizflow_sales_db';
SET @table_name = 'orders';
SET @col_name = 'customer_phone';

SELECT COUNT(*) INTO @col_exists FROM information_schema.columns
 WHERE table_schema = @schema_name AND table_name = @table_name AND column_name = @col_name;

SET @sql = IF(@col_exists = 0,
  CONCAT('ALTER TABLE `', @schema_name, '`.`', @table_name, '` ADD COLUMN `', @col_name, '` VARCHAR(255) DEFAULT NULL;'),
  'SELECT "column exists" AS info;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Backfill existing orders from customers table in customer DB (assumes both DBs on same server)
UPDATE `bizflow_sales_db`.`orders` o
JOIN `bizflow_customer_db`.`customers` c ON o.customer_id = c.id
SET o.customer_phone = c.phone
WHERE o.customer_id IS NOT NULL AND (o.customer_phone IS NULL OR o.customer_phone = '');

-- End of migration
