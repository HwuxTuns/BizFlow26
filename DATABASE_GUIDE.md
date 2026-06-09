# 🗄️ Hướng Dẫn Quản Lý Database BizFlow

## ✅ Database Đã Import

Tất cả databases đã được import vào MySQL container:

- ✅ `bizflow_auth_db` - Authentication & Users
- ✅ `bizflow_catalog_db` - Products & Categories  
- ✅ `bizflow_customer_db` - Customers & Points
- ✅ `bizflow_inventory_db` - Inventory & Shelves
- ✅ `bizflow_promotion_db` - Promotions & Discounts
- ✅ `bizflow_report_db` - Reports & Analytics
- ✅ `bizflow_sales_db` - Orders & Payments

---

## 🔒 Data Persistence (Lưu Dữ Liệu Vĩnh Viễn)

### Docker Volume

Data được lưu trong Docker volume: **`mysql_data`**

```yaml
volumes:
  - mysql_data:/var/lib/mysql  # ← Data lưu ở đây
```

### ✅ Data KHÔNG MẤT khi:

- ✅ `docker compose down` - Tắt containers
- ✅ `docker compose restart` - Restart
- ✅ `docker compose up -d --build` - Rebuild images

### ❌ Data SẼ MẤT khi:

- ❌ `docker compose down -v` - Xóa volumes
- ❌ `docker volume rm mysql_data` - Xóa volume thủ công
- ❌ Xóa Docker Desktop data

---

## 🚀 Các Tình Huống Thường Gặp

### 1️⃣ Restart Docker (Data KHÔNG mất)

```bash
docker compose down
docker compose up -d
```

✅ Data vẫn còn trong volume `mysql_data`

### 2️⃣ Rebuild Services (Data KHÔNG mất)

```bash
docker compose up -d --build
```

✅ Chỉ rebuild code, data vẫn còn

### 3️⃣ Xóa Containers & Volumes (Data MẤT)

```bash
docker compose down -v  # ⚠️ CẢNH BÁO: Mất data!
```

❌ Nếu chạy lệnh này, cần restore lại database

### 4️⃣ Restore Database (Khi Mất Data)

```bash
./restore-databases.sh
```

Script này sẽ:
- Tạo lại databases
- Import lại tất cả data từ `db/*.sql`

---

## 📦 Backup Database

### Tự Động Backup

Chạy script có sẵn:

```bash
./backup-database.ps1  # Windows PowerShell
# Hoặc
docker exec bizflow-mysql mysqldump -uroot -p123456 --all-databases > backup.sql
```

### Manual Backup

Backup từng database:

```bash
docker exec bizflow-mysql mysqldump -uroot -p123456 bizflow_auth_db > backup_auth.sql
docker exec bizflow-mysql mysqldump -uroot -p123456 bizflow_catalog_db > backup_catalog.sql
# ...
```

---

## 🔍 Kiểm Tra Database

### Xem Danh Sách Databases

```bash
docker exec bizflow-mysql mysql -uroot -p123456 -e "SHOW DATABASES LIKE 'bizflow%';"
```

### Kiểm Tra Tables

```bash
docker exec bizflow-mysql mysql -uroot -p123456 -e "USE bizflow_auth_db; SHOW TABLES;"
```

### Xem Dữ Liệu

```bash
docker exec bizflow-mysql mysql -uroot -p123456 -e "SELECT * FROM bizflow_auth_db.users LIMIT 5;"
```

### Truy Cập MySQL Shell

```bash
docker exec -it bizflow-mysql mysql -uroot -p123456
```

---

## 🌐 Kết Nối qua phpMyAdmin

**URL:** http://localhost:8088

**Thông tin đăng nhập:**
- Server: `mysql`
- Username: `root`
- Password: `123456`

---

## ⚙️ Cấu Hình MySQL

### Thông Tin Kết Nối

```
Host: localhost
Port: 3307  # Mapped từ 3306
User: root
Password: 123456
```

### Từ Services (trong Docker network)

```
Host: mysql  # Container name
Port: 3306   # Internal port
User: root
Password: 123456
```

---

## 🛠️ Troubleshooting

### 1. Database bị mất sau khi restart?

**Nguyên nhân:** Volume bị xóa  
**Giải pháp:**

```bash
./restore-databases.sh
```

### 2. MySQL không kết nối được?

**Kiểm tra container:**

```bash
docker ps | grep mysql
docker logs bizflow-mysql
```

**Restart MySQL:**

```bash
docker compose restart mysql
```

### 3. Import thất bại?

**Xóa và import lại:**

```bash
docker exec bizflow-mysql mysql -uroot -p123456 -e "DROP DATABASE bizflow_auth_db;"
docker exec -i bizflow-mysql mysql -uroot -p123456 -e "CREATE DATABASE bizflow_auth_db;"
docker exec -i bizflow-mysql mysql -uroot -p123456 bizflow_auth_db < db/bizflow_auth_db.sql
```

### 4. Kiểm tra volume còn data không?

```bash
docker volume inspect mysql_data
docker volume ls | grep mysql
```

---

## 📝 Lưu Ý Quan Trọng

1. ✅ **KHÔNG chạy** `docker compose down -v` trừ khi muốn xóa hết data
2. ✅ **Backup thường xuyên** trước khi update/rebuild
3. ✅ **Test restore script** để đảm bảo hoạt động
4. ✅ **Check volume size** định kỳ: `docker system df -v`

---

## 🎯 Quick Commands

```bash
# Restore database (khi mất data)
./restore-databases.sh

# Backup all databases
docker exec bizflow-mysql mysqldump -uroot -p123456 --all-databases > backup.sql

# Start services
docker compose up -d

# Stop services (data vẫn còn)
docker compose down

# Check MySQL logs
docker logs bizflow-mysql

# Access MySQL shell
docker exec -it bizflow-mysql mysql -uroot -p123456
```

---

## 📊 Volume Management

```bash
# Liệt kê volumes
docker volume ls

# Xem chi tiết volume
docker volume inspect mysql_data

# Backup volume (advanced)
docker run --rm -v mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_data_backup.tar.gz /data

# Restore volume (advanced)
docker run --rm -v mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql_data_backup.tar.gz -C /
```

---

**🎉 Hệ thống database của bạn đã sẵn sàng và được bảo vệ!**
