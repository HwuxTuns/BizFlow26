#!/usr/bin/env bash
set -euo pipefail

MYSQL_CONTAINER="bizflow-mysql"
MYSQL_USER="root"
MYSQL_PASSWORD="123456"
BACKUP_ROOT="db/backups/current"
DB_BACKUP_DIR="$BACKUP_ROOT/databases"
IMAGE_BACKUP_DIR="$BACKUP_ROOT/product-images"

echo "=== BizFlow Restore Current Backup ==="

if ! docker ps --filter "name=${MYSQL_CONTAINER}" --format "{{.Names}}" | grep -q "${MYSQL_CONTAINER}"; then
  echo "MySQL container chua chay. Hay chay: docker compose up -d"
  exit 1
fi

echo "Dang import database..."
for sql in "$DB_BACKUP_DIR"/bizflow_*.sql; do
  [ -e "$sql" ] || continue
  if [[ "$(basename "$sql")" == "bizflow_all_databases.sql" ]]; then
    continue
  fi
  echo "  -> $(basename "$sql")"
  docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < "$sql"
done

mkdir -p BizFlow.Frontend/assets/img/img_sanpham BizFlow.Frontend/assets/data

if [ -d "$IMAGE_BACKUP_DIR/img_sanpham" ]; then
  cp -r "$IMAGE_BACKUP_DIR/img_sanpham"/* BizFlow.Frontend/assets/img/img_sanpham/
fi
if [ -f "$IMAGE_BACKUP_DIR/data/product-image-files.json" ]; then
  cp "$IMAGE_BACKUP_DIR/data/product-image-files.json" BizFlow.Frontend/assets/data/product-image-files.json
fi

echo "Restore hoan tat."
docker exec "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SHOW DATABASES LIKE 'bizflow%';"
