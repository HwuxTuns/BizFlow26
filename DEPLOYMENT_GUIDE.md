# BizFlow Deployment Guide

## Trả lời thắc mắc của Thầy về localhost API

### ❓ Câu hỏi: "Hiện tại các port đều gán localhost, sau này deploy lên production có phải ngồi chỉnh từng port không?"

### ✅ Câu trả lời:

**KHÔNG cần chỉnh thủ công từng port ạ.** Em sử dụng **Environment Variables** để tự động thay đổi theo môi trường.

---

## 1. Backend Services (Java) ✅

### Code hiện tại:
```yaml
# application.yml
catalog:
  base-url: ${CATALOG_SERVICE_URL:http://localhost:8083}
```

### Docker Compose (Development):
```yaml
environment:
  CATALOG_SERVICE_URL: http://catalog-service:8083  # Container name
```

### Production deployment:
```bash
# Chỉ cần set env var, không sửa code
export CATALOG_SERVICE_URL=https://catalog.bizflow.com
export INVENTORY_SERVICE_URL=https://inventory.bizflow.com
```

---

## 2. Frontend (JavaScript) ✅

### Code hiện tại đã xử lý tự động:
```javascript
function resolveApiBase() {
    // 1. Ưu tiên config từ window.API_BASE_URL
    const configured = window.API_BASE_URL;
    if (configured) return configured;
    
    // 2. Development: dùng localhost
    if (window.location.hostname === 'localhost') {
        return 'http://localhost:8000/api';
    }
    
    // 3. Production: tự động lấy domain hiện tại
    return `${window.location.origin}/api`;
}
```

### Kết quả:
- **Development:** `http://localhost:8000/api`
- **Production:** `https://bizflow.com/api` (tự động!)

---

## 3. Cách triển khai Production

### Option 1: Docker + Environment File
```bash
# .env.production
CATALOG_SERVICE_URL=https://catalog.bizflow.com
INVENTORY_SERVICE_URL=https://inventory.bizflow.com
DB_HOST=prod-db.amazonaws.com
```

```bash
# Deploy
docker-compose --env-file .env.production up -d
```

### Option 2: Kubernetes
```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bizflow-config
data:
  CATALOG_SERVICE_URL: "https://catalog.bizflow.com"
  INVENTORY_SERVICE_URL: "https://inventory.bizflow.com"
```

### Option 3: Cloud Services
- **AWS:** Systems Manager Parameter Store
- **Azure:** Key Vault
- **GCP:** Secret Manager

---

## 4. Demo cho Thầy

### Scenario: Chuyển từ Dev → Production

**Development (localhost):**
```bash
DB_HOST=mysql
CATALOG_SERVICE_URL=http://catalog-service:8083
```

**Production (chỉ thay env vars):**
```bash
DB_HOST=prod-mysql.rds.amazonaws.com
CATALOG_SERVICE_URL=https://catalog.bizflow.com
```

**Không cần:**
- ❌ Sửa code
- ❌ Rebuild image
- ❌ Chỉnh từng file

**Chỉ cần:**
- ✅ Thay file `.env`
- ✅ Restart services
- ✅ Done!

---

## 5. Best Practices

### ✅ ĐÚNG (Em đã làm):
- Environment variables cho mọi config
- Auto-detect môi trường ở frontend
- Centralized configuration

### ❌ SAI (không làm):
- Hardcode URLs trong code
- Build riêng cho từng môi trường
- Manual config cho từng service

---

## Kết luận

> "Dạ thưa thầy, em không cần chỉnh từng port thủ công ạ. 
> 
> Em sử dụng **Environment Variables** và **Auto-detection** để hệ thống tự động thay đổi config theo môi trường.
> 
> Khi deploy production, em chỉ cần:
> 1. Tạo file `.env.production` với các URLs mới
> 2. Chạy `docker-compose --env-file .env.production up -d`
> 3. Không cần sửa 1 dòng code nào ạ."

---

**Files tham khảo:**
- `.env.example` - Template cho environment variables
- `docker-compose.yml` - Cấu hình Docker
- `owner-promotions-temp.html` - Frontend auto-detection
