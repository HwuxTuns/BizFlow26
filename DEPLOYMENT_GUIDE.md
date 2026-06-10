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

---

## 6. Hướng dẫn triển khai Hybrid Cloud (AWS / GCP) ☁️

Mục tiêu cuối cùng là vận hành BizFlow dưới dạng kiến trúc **Hybrid Cloud**, nơi các dịch vụ có thể co giãn linh hoạt và tách biệt phần lưu trữ dữ liệu (Stateful) ra khỏi tính toán (Stateless) nhằm tăng hiệu năng tối đa.

### 6.1 Tách các Container Infrastructure sang Managed Services

Thay vì chạy toàn bộ 25 container trên một server (rất dễ sập và tốn tài nguyên), hãy chuyển các Container hạ tầng sang các dịch vụ đám mây tự quản lý:

| Dịch vụ hạ tầng | AWS Managed Service | GCP Managed Service | Thay đổi trong `.env` |
| :--- | :--- | :--- | :--- |
| **MySQL Database** | Amazon RDS (MySQL) | Cloud SQL (MySQL) | `DB_HOST=tên-host-rds.amazonaws.com` |
| **Redis Cache** | Amazon ElastiCache | Memorystore for Redis | `REDIS_HOST=tên-endpoint-elasticache` |
| **RabbitMQ** | Amazon MQ | CloudAMQP (Partner) | `RABBITMQ_HOST=tên-host-mq` |
| **Kafka / Zookeeper** | Amazon MSK | Managed Service for Apache Kafka | `KAFKA_BOOTSTRAP_SERVERS=endpoints` |

> [!TIP]
> Việc chuyển đổi này giúp bạn **giảm từ 25 container xuống chỉ còn ~13 container stateless** tự quản lý (Nginx, Gateway, và các microservices), giúp tiết kiệm đến **50% lượng RAM & CPU** cần thiết cho máy chủ chạy App!

### 6.2 Mô hình Kiến trúc Deploy Stateless App (Gateway / Microservices)

#### Mô hình 1: Đám mây lai (Hybrid Edge - Cửa hàng & Cloud)
- **Local Edge (Cửa hàng):** Chạy máy ảo/Mini PC chứa Frontend Nginx và API Gateway để nhân viên bán hàng nhanh, không sợ mất mạng.
- **Cloud Backend (AWS/GCP):** Các Database và Core Microservices nằm trên Cloud. Các đơn hàng từ local edge được đồng bộ thông qua VPN/Direct Connect trực tiếp lên Kafka/RabbitMQ trên Cloud.

#### Mô hình 2: Triển khai Kubernetes (EKS / GKE)
Triển khai file yaml lên Kubernetes Service (EKS/GKE). Gateway sẽ tự động phát hiện (Service Discovery) các pod microservices khác trong cụm K8s:
- Sử dụng **Ingress Controller** thay thế cho Nginx Frontend để tự động cấp phát SSL và Route traffic.
- Sử dụng **Horizontal Pod Autoscaler (HPA)** để tự động nhân bản các microservices có tải nặng (như `sales-service`) khi lượng đơn hàng tăng đột biến.

