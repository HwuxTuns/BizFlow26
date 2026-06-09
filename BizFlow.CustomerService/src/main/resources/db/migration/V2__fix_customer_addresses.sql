-- Update incorrect addresses in customers table
-- Fix addresses that are missing house number or have incorrect province

-- Fix specific incorrect address
UPDATE customers
SET address = REPLACE(address, 'Phan Văn Hớn, Khu phố 23, Phường Đông Hưng Thuận, Thuận An', '102 Phan Văn Hớn, Phường Tân Thới Nhất, Quận 12, Hồ Chí Minh')
WHERE address LIKE '%Phan Văn Hớn, Khu phố 23, Phường Đông Hưng Thuận, Thuận An%';

-- Fix the current incorrect address
UPDATE customers
SET address = REPLACE(address, 'Phan Văn Hớn, Phường Đông Hưng Thuận, Thuận An', '102 Phan Văn Hớn, Tân Thới Nhất, Đông Hưng Thuận, Hồ Chí Minh')
WHERE address LIKE '%Phan Văn Hớn, Phường Đông Hưng Thuận, Thuận An%';

-- Remove "Thuận An" from addresses in Ho Chi Minh City
UPDATE customers
SET address = REPLACE(address, ', Thuận An', '')
WHERE address LIKE '%, Thuận An%' AND address LIKE '%Hồ Chí Minh%';

-- Replace "Khu phố 23" with "Tân Thới Nhất" if applicable
UPDATE customers
SET address = REPLACE(address, 'Khu phố 23', 'Tân Thới Nhất')
WHERE address LIKE '%Khu phố 23%' AND address LIKE '%Đông Hưng Thuận%';

-- General fix for addresses missing 'Hồ Chí Minh'
UPDATE customers
SET address = CONCAT(address, ', Hồ Chí Minh')
WHERE address NOT LIKE '%Hồ Chí Minh%'
  AND address NOT LIKE '%Ho Chi Minh%'
  AND address LIKE '%, %'  -- Ensure it's a proper address with commas
  AND LENGTH(address) > 10;  -- Avoid very short addresses