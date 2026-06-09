#!/bin/bash

echo "🧹 Dọn dẹp Docker..."

# Stop tất cả containers
echo "⏹️  Stop containers..."
docker compose down -v

# Xóa tất cả containers
echo "🗑️  Xóa containers..."
docker container prune -f

# Xóa volumes không dùng
echo "💾 Xóa volumes..."
docker volume prune -f

# Xóa networks không dùng  
echo "🌐 Xóa networks..."
docker network prune -f

# Xóa images không dùng (bỏ comment nếu muốn)
# echo "🖼️  Xóa images..."
# docker image prune -af

# Xóa build cache
echo "🗂️  Xóa build cache..."
docker builder prune -af

echo "✅ Hoàn thành! Docker đã được dọn dẹp sạch sẽ."
echo "💡 Bạn có thể tắt máy và chạy lại bằng: docker compose up -d"
