#!/bin/bash
# Quick Setup Script for Admin Services
# Chạy script này để setup nhanh Admin Services

echo "=========================================="
echo "🚀 Admin Services Quick Setup"
echo "=========================================="

# 1. Create databases and initialize data
echo ""
echo "📊 Step 1: Initializing databases..."
mysql -u root -p$1 << EOF
source db/07_admin_services_init.sql;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Databases created successfully"
else
    echo "❌ Database initialization failed"
    exit 1
fi

# 2. Build all modules
echo ""
echo "🔨 Step 2: Building all modules..."
mvn clean install -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully"
else
    echo "❌ Build failed"
    exit 1
fi

# 3. Summary
echo ""
echo "=========================================="
echo "✨ Setup Complete!"
echo "=========================================="
echo ""
echo "📝 Next steps:"
echo "1. Update application.properties in each service"
echo "2. Run: java -jar BizFlow.AdminUserService/target/admin-user-service-*.jar"
echo "3. Run: java -jar BizFlow.AdminProductService/target/admin-product-service-*.jar"
echo "4. Run: java -jar BizFlow.AdminOrderService/target/admin-order-service-*.jar"
echo "5. Run: java -jar BizFlow.AdminReportService/target/admin-report-service-*.jar"
echo "6. Run: java -jar BizFlow.Gateway/target/gateway-*.jar"
echo ""
echo "🌐 Access:"
echo "   http://localhost:3000/admin/users.html"
echo "   http://localhost:3000/admin/products.html"
echo "   http://localhost:3000/admin/orders.html"
echo "   http://localhost:3000/admin/reports.html"
echo ""
echo "=========================================="
