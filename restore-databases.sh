#!/bin/bash

# Quick Database Restore Script
# Sử dụng khi cần khôi phục lại database sau khi docker compose down

set -e

MYSQL_CONTAINER="bizflow-mysql"
MYSQL_USER="root"
MYSQL_PASSWORD="123456"

echo "🔄 Restoring BizFlow Databases..."
echo ""

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
sleep 5

# Create databases
echo "📦 Creating databases..."
docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD -e "
CREATE DATABASE IF NOT EXISTS bizflow_auth_db;
CREATE DATABASE IF NOT EXISTS bizflow_catalog_db;
CREATE DATABASE IF NOT EXISTS bizflow_customer_db;
CREATE DATABASE IF NOT EXISTS bizflow_inventory_db;
CREATE DATABASE IF NOT EXISTS bizflow_promotion_db;
CREATE DATABASE IF NOT EXISTS bizflow_report_db;
CREATE DATABASE IF NOT EXISTS bizflow_sales_db;
" 2>/dev/null

echo "✅ Databases created"
echo ""

# Import data
databases=(
    "bizflow_auth_db"
    "bizflow_catalog_db"
    "bizflow_customer_db"
    "bizflow_inventory_db"
    "bizflow_promotion_db"
    "bizflow_report_db"
    "bizflow_sales_db"
)

for db in "${databases[@]}"; do
    echo "📥 Importing ${db}..."
    docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD $db < "db/${db}.sql" 2>/dev/null
    echo "✅ ${db} imported"
done

echo ""
echo "📥 Importing shelves data..."
docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD bizflow_inventory_db < "db/shelves.sql" 2>/dev/null || echo "ℹ️  Shelves already exist"

echo ""
echo "🎉 Database restore completed!"
echo ""
echo "📊 Available databases:"
docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW DATABASES LIKE 'bizflow%';" 2>/dev/null

echo ""
echo "💡 Tip: Data is stored in Docker volume 'mysql_data' and will persist across restarts!"
