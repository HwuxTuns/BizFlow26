# Hướng Dẫn Triển Khai BizFlow Lên AWS Production

Tài liệu này hướng dẫn chi tiết từng bước đóng gói các microservices, đẩy lên Amazon ECR, chạy Terraform để khởi tạo hạ tầng và vận hành hệ thống BizFlow trên AWS.

---

## 🛠️ Bước 1: Chuẩn Bị Công Cụ & Quyền Truy Cập
Đảm bảo máy của bạn đã cài đặt các công cụ sau:
1. **AWS CLI** (Đã chạy lệnh `aws configure` cấu hình Access Key ID & Secret Access Key).
2. **Terraform CLI** (Đã thêm vào biến môi trường PATH).
3. **Docker Desktop** (Đang chạy để build images).

---

## 📦 Bước 2: Tạo Kho Chứa Ảnh (Amazon ECR) & Đẩy Image Lên
Các microservices cần được build thành Docker image và lưu trên Amazon ECR. 

Chạy các lệnh sau để tự động tạo ECR repositories và đẩy các image lên (thay thế `<ACCOUNT_ID>` và `<REGION>` bằng thông tin tài khoản AWS của bạn, ví dụ: `123456789012` và `ap-southeast-1`):

```bash
# 1. Đăng nhập Docker vào Amazon ECR
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

# 2. Danh sách các dịch vụ cần build
SERVICES=(
  "gateway" "frontend" "catalog-service" "sales-service" "inventory-service" 
  "customer-service" "authentication-service" "promotion-service" "report-service" 
  "ai-service" "admin-user-service" "admin-product-service" "admin-order-service" 
  "admin-report-service"
)

# 3. Tạo Repository và Build/Push từng service
for SERVICE in "${SERVICES[@]}"; do
  # Tạo repo trên ECR (bỏ qua nếu đã tồn tại)
  aws ecr create-repository --repository-name "bizflow/$SERVICE" --region <REGION> || true
  
  # Xác định thư mục chứa Dockerfile tương ứng
  DIR=""
  if [ "$SERVICE" == "gateway" ]; then DIR="./BizFlow.Gateway"
  elif [ "$SERVICE" == "frontend" ]; then DIR="./BizFlow.Frontend"
  elif [ "$SERVICE" == "ai-service" ]; then DIR="./BizFlow.AIService"
  elif [ "$SERVICE" == "catalog-service" ]; then DIR="./BizFlow.CatalogService"
  elif [ "$SERVICE" == "sales-service" ]; then DIR="./BizFlow.SalesService"
  elif [ "$SERVICE" == "inventory-service" ]; then DIR="./BizFlow.InventoryService"
  elif [ "$SERVICE" == "customer-service" ]; then DIR="./BizFlow.CustomerService"
  elif [ "$SERVICE" == "authentication-service" ]; then DIR="./BizFlow.AuthenticationService"
  elif [ "$SERVICE" == "promotion-service" ]; then DIR="./BizFlow.PromotionService"
  elif [ "$SERVICE" == "report-service" ]; then DIR="./BizFlow.ReportService"
  elif [ "$SERVICE" == "admin-user-service" ]; then DIR="./BizFlow.AdminUserService"
  elif [ "$SERVICE" == "admin-product-service" ]; then DIR="./BizFlow.AdminProductService"
  elif [ "$SERVICE" == "admin-order-service" ]; then DIR="./BizFlow.AdminOrderService"
  elif [ "$SERVICE" == "admin-report-service" ]; then DIR="./BizFlow.AdminReportService"
  fi
  
  echo ">>> Đang build service: $SERVICE trong thư mục: $DIR"
  docker build -t "bizflow/$SERVICE:latest" "$DIR"
  
  # Tag & Push lên ECR
  docker tag "bizflow/$SERVICE:latest" "<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/bizflow/$SERVICE:latest"
  docker push "<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/bizflow/$SERVICE:latest"
done
```

---

## 🚀 Bước 3: Triển Khai Hạ Tầng Bằng Terraform
1. Di chuyển vào thư mục terraform:
   ```bash
   cd terraform
   ```
2. Khởi tạo Terraform provider và các plugins:
   ```bash
   terraform init
   ```
3. Xem trước kế hoạch triển khai (kiểm tra các tài nguyên sắp được tạo):
   ```bash
   terraform plan
   ```
4. Áp dụng triển khai hạ tầng lên AWS:
   ```bash
   terraform apply -auto-approve
   ```
   *Quá trình này có thể mất từ 10 - 20 phút để khởi tạo RDS MySQL, Amazon MQ RabbitMQ và MSK Serverless.*

5. Sau khi thành công, Terraform sẽ xuất ra URL Load Balancer của bạn ở phần output:
   `alb_dns_name = "bizflow-alb-xxxxxxxxx.ap-southeast-1.elb.amazonaws.com"`

---

## 🗄️ Bước 4: Khởi Tạo Các Databases & Dữ Liệu Ban Đầu
Khi RDS MySQL mới được khởi tạo, nó chỉ có sẵn 1 database mặc định là `bizflow_catalog_db`. Để khởi tạo 6 database còn lại và import dữ liệu:

1. Thiết lập cổng bảo mật qua Bastion Host hoặc cấp quyền truy cập tạm thời vào cổng 3306 của RDS cho IP của bạn (Security Group `bizflow-db-sg`).
2. Kết nối tới RDS instance qua endpoint từ Terraform output bằng DB Client (DBeaver, MySQL Workbench) với:
   * **Host:** `<rds_endpoint>`
   * **Username:** `root`
   * **Password:** `<mật khẩu bạn cấu hình>`
3. Chạy câu lệnh SQL sau để tạo các database:
   ```sql
   CREATE DATABASE IF NOT EXISTS bizflow_auth_db;
   CREATE DATABASE IF NOT EXISTS bizflow_customer_db;
   CREATE DATABASE IF NOT EXISTS bizflow_inventory_db;
   CREATE DATABASE IF NOT EXISTS bizflow_promotion_db;
   CREATE DATABASE IF NOT EXISTS bizflow_report_db;
   CREATE DATABASE IF NOT EXISTS bizflow_sales_db;
   ```
4. Thực hiện chạy các file script `.sql` tương ứng nằm trong thư mục `./db/` của dự án để khôi phục cấu trúc bảng và dữ liệu mẫu.

---

## 🧹 Xóa Tài Nguyên (Khi Không Còn Sử Dụng)
Để tránh phát sinh chi phí không mong muốn trên tài khoản AWS của bạn, hãy xóa sạch các tài nguyên sau khi thử nghiệm xong:
```bash
cd terraform
terraform destroy -auto-approve
```
**Lưu ý:** Lệnh này sẽ xóa toàn bộ dữ liệu lưu trên RDS, EFS và ElastiCache, hãy chắc chắn đã backup nếu cần thiết.
