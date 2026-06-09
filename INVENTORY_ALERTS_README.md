# HỆ THỐNG THÔNG BÁO TỒN KHO & TỒN KỆ

## 📋 TỔNG QUAN

Hệ thống thông báo tồn kho & tồn kệ được triển khai **KHÔNG sử dụng Worker Service**, 
hoàn toàn dựa trên logic runtime (Controller + Service + Repository).

---

## 🎯 NGHIỆP VỤ

### 1. QUY TẮC THÔNG BÁO

#### A. SẢN PHẨM TRÊN KỆ (shelves)
- **Kiểm tra**: Realtime mỗi khi load trang
- **WARNING** (Màu vàng): `quantity < 10`
- **DANGER** (Màu đỏ): `quantity < 5`

#### B. SẢN PHẨM TRONG KHO (inventory_stocks)
- **Kiểm tra**: Theo thời gian (giả lập 7h mỗi ngày)
- **WARNING** (Màu vàng): `quantity < 20`
- **DANGER** (Màu đỏ): `quantity < 10`
- **Logic**: Chỉ hiển thị nếu `lastCheckedDate < today`
  - Sử dụng localStorage để lưu `inventory_last_checked`
  - Khi user vào trang → kiểm tra date → nếu khác ngày thì hiển thị

---

## 📦 CÁC FILE ĐÃ TẠO/CHỈNH SỬA

### Backend (InventoryService)

#### 1. **InventoryAlertDTO.java** (MỚI)
```
BizFlow.InventoryService/src/main/java/com/example/bizflow/dto/InventoryAlertDTO.java
```
- DTO chứa thông tin thông báo
- Fields: `type`, `level`, `productId`, `productName`, `productCode`, `quantity`, `message`
- `type`: "SHELF" hoặc "WAREHOUSE"
- `level`: "WARNING" hoặc "DANGER"

#### 2. **InventoryAlertService.java** (MỚI)
```
BizFlow.InventoryService/src/main/java/com/example/bizflow/service/InventoryAlertService.java
```
- Service chính xử lý logic thông báo
- Methods:
  - `getAllAlerts(LocalDate lastCheckedDate)` - Lấy tất cả thông báo
  - `getShelfAlerts()` - Lấy thông báo từ kệ (realtime)
  - `getWarehouseAlerts()` - Lấy thông báo từ kho
- **Fail-safe**: Nếu có lỗi → return empty list, KHÔNG throw exception

#### 3. **InventoryController.java** (CẬP NHẬT)
```
BizFlow.InventoryService/src/main/java/com/example/bizflow/controller/InventoryController.java
```
- Thêm dependency: `InventoryAlertService`
- Thêm endpoint mới:
  ```java
  GET /api/inventory/alerts?lastChecked=yyyy-MM-dd
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  ```
- Return: `List<InventoryAlertDTO>`

---

### Frontend

#### 4. **owner-inventory-alerts.html** (MỚI)
```
BizFlow.Frontend/pages/owner-inventory-alerts.html
```
- Trang hiển thị danh sách thông báo
- Features:
  - **Statistics Cards**: Tổng số thông báo, chia theo kệ/kho/nguy hiểm
  - **Filter Buttons**: Lọc theo tất cả/kệ/kho/nguy hiểm/cảnh báo
  - **Alert List**: Danh sách thông báo với:
    - Icon phân biệt WARNING (⚠️) và DANGER (🚨)
    - Màu nền: WARNING (vàng), DANGER (đỏ)
    - Thông tin: Tên SP, mã SP, số lượng, vị trí
  - **Auto Refresh**: Tự động reload mỗi 5 phút
  - **localStorage**: Lưu `inventory_last_checked` để giả lập check hàng ngày

#### 5. **Các trang owner-*.html** (CẬP NHẬT)
Đã cập nhật navigation sidebar cho 7 trang:
- owner-dashboard.html
- owner-reports.html
- owner-products.html
- owner-categories.html
- owner-inventory.html
- owner-users.html
- owner-promotions.html

**Thay đổi**: Thêm link "Thông báo Tồn kho" giữa "Khuyến mãi" và "Thiết lập"

---

## 🔌 API ENDPOINT

### GET /api/inventory/alerts

**URL**: `http://localhost:8084/api/inventory/alerts?lastChecked=2026-02-03`

**Method**: GET

**Auth**: Bearer Token (OWNER hoặc ADMIN)

**Query Params**:
- `lastChecked` (optional): Ngày kiểm tra cuối cùng (format: yyyy-MM-dd)
  - Nếu null hoặc < today → hiển thị thông báo kho
  - Thông báo kệ luôn hiển thị realtime

**Response**: 
```json
[
  {
    "type": "SHELF",
    "level": "DANGER",
    "productId": 123,
    "productName": "Nước ngọt Coca Cola",
    "productCode": "CC001",
    "quantity": 3,
    "message": "Sản phẩm 'Nước ngọt Coca Cola' trên kệ chỉ còn 3 lon"
  },
  {
    "type": "WAREHOUSE",
    "level": "WARNING",
    "productId": 456,
    "productName": "Bánh quy Oreo",
    "productCode": "OR002",
    "quantity": 15,
    "message": "Sản phẩm 'Bánh quy Oreo' trong kho chỉ còn 15 gói"
  }
]
```

---

## 🎨 UI/UX

### Màu sắc
- **WARNING**: 
  - Background: `#fffbeb` (vàng nhạt)
  - Border: `#fbbf24` (vàng)
  - Icon: ⚠️
  
- **DANGER**: 
  - Background: `#fef2f2` (đỏ nhạt)
  - Border: `#ef4444` (đỏ)
  - Icon: 🚨

### Statistics Cards
- Tổng cảnh báo
- Cảnh báo Kệ (màu vàng)
- Cảnh báo Kho (màu vàng)
- Cảnh báo Nguy hiểm (màu đỏ)

### Filter
- Tất cả
- Kệ
- Kho
- Nguy hiểm
- Cảnh báo

---

## ✅ AN TOÀN & ỔN ĐỊNH

### 1. Không Break Code Cũ
- Chỉ thêm service mới, không sửa service cũ
- Chỉ thêm endpoint mới, không sửa endpoint cũ
- Navigation được thêm vào tất cả trang để đồng nhất

### 2. Fail-Safe
- Service có try-catch → return empty list nếu lỗi
- Frontend có fallback UI khi API lỗi
- Không throw exception không cần thiết

### 3. Sử Dụng Đúng Entity/Table
- ✅ Dùng đúng `inventory_stocks` table (KHÔNG phải warehouse)
- ✅ Dùng đúng `shelves` table
- ✅ Dùng đúng field names: `quantity`, `stock`, `productId`

### 4. Performance
- Chỉ query database khi cần
- Frontend cache lastCheckedDate trong localStorage
- Auto refresh 5 phút (không quá thường xuyên)

---

## 🚀 CÁCH SỬ DỤNG

### 1. Truy cập trang
- Login với role OWNER hoặc ADMIN
- Vào sidebar → Click "🔔 Thông báo Tồn kho"
- URL: `http://localhost/pages/owner-inventory-alerts.html`

### 2. Xem thông báo
- Tự động load khi vào trang
- Hiển thị thống kê tổng quan
- Danh sách thông báo chi tiết

### 3. Filter
- Click nút filter để xem theo loại
- Tất cả / Kệ / Kho / Nguy hiểm / Cảnh báo

### 4. Auto Refresh
- Tự động refresh mỗi 5 phút
- Có thể F5 để refresh thủ công

---

## 📝 LƯU Ý KỸ THUẬT

### 1. Giả lập "Check hàng ngày"
- Backend KHÔNG dùng Scheduler/Worker
- Frontend dùng `localStorage.getItem('inventory_last_checked')`
- Khi vào trang:
  - Nếu `lastChecked === null` hoặc `lastChecked < today`
  - → Gọi API với param `lastChecked`
  - → Backend trả thông báo kho
  - → Lưu `today` vào localStorage
- Lần sau vào trong cùng ngày → không hiển thị thông báo kho nữa

### 2. Thông báo Kệ Realtime
- Luôn kiểm tra mỗi lần load trang
- KHÔNG phụ thuộc vào `lastChecked`

### 3. Query Database
- Service query trực tiếp từ Repository
- Không cache trong memory
- Mỗi request mới = query mới

---

## 🔍 TESTING

### Test Case 1: Thông báo Kệ
1. Tạo sản phẩm có `quantity < 10` trên kệ
2. Vào trang thông báo
3. Kỳ vọng: Hiển thị WARNING/DANGER

### Test Case 2: Thông báo Kho
1. Tạo sản phẩm có `stock < 20` trong kho
2. Clear localStorage hoặc set `inventory_last_checked` = ngày hôm qua
3. Vào trang thông báo
4. Kỳ vọng: Hiển thị WARNING/DANGER cho cả kệ và kho

### Test Case 3: Đã check hôm nay
1. Vào trang thông báo lần đầu
2. localStorage lưu today
3. Vào lại trong ngày
4. Kỳ vọng: Chỉ hiển thị thông báo kệ, KHÔNG hiển thị kho

### Test Case 4: Filter
1. Vào trang có nhiều thông báo
2. Click filter "Kệ" → chỉ hiển thị kệ
3. Click filter "Nguy hiểm" → chỉ hiển thị DANGER

---

## 🎉 HOÀN THÀNH

Hệ thống đã sẵn sàng sử dụng!

- ✅ Backend: InventoryAlertService + API endpoint
- ✅ Frontend: Trang thông báo + Navigation
- ✅ Logic nghiệp vụ: Đúng yêu cầu kệ & kho
- ✅ Không Worker Service
- ✅ Fail-safe & Stable
- ✅ Không break code cũ

**Tác giả**: GitHub Copilot  
**Ngày**: 03/02/2026
