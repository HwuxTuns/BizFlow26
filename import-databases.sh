#!/bin/bash

# Import All BizFlow Databases
# This script imports all database SQL files into MySQL container

set -e

MYSQL_CONTAINER="bizflow-mysql"
MYSQL_USER="root"
MYSQL_PASSWORD="123456"

echo "🔄 Starting database import..."
echo ""

# List of databases to import
databases=(
    "bizflow_auth_db"
    "bizflow_catalog_db"
    "bizflow_customer_db"
    "bizflow_inventory_db"
    "bizflow_promotion_db"
    "bizflow_report_db"
    "bizflow_sales_db"
)

# Import each database
for db in "${databases[@]}"; do
    echo "📦 Importing ${db}..."
    docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD < "db/${db}.sql" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ ${db} imported successfully"
    else
        echo "❌ Failed to import ${db}"
    fi
    echo ""
done

# Import shelves data
echo "📦 Importing shelves data..."
docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD bizflow_inventory_db < "db/shelves.sql" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Shelves data imported successfully"
else
    echo "❌ Failed to import shelves data"
fi

echo ""
echo "🎉 Database import completed!"
echo ""
echo "📊 Verifying databases..."
docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW DATABASES;" 2>/dev/null | grep bizflow

echo ""
echo "✨ All done! Your data is now persistent in Docker volume: mysql_data"
