@echo off
REM Admin Services Quick Setup for Windows
REM Chạy script này để setup nhanh Admin Services

setlocal

REM Prefer repo-local JDK (team-friendly) if present
set "REPO_JDK=%~dp0.jdk\jdk-21.0.8"
if exist "%REPO_JDK%\bin\java.exe" (
    set "JAVA_HOME=%REPO_JDK%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo ==========================================
echo.🚀 Admin Services Quick Setup (Windows)
echo ==========================================

REM 1. Create databases
echo.
echo 📊 Step 1: Initializing databases...
mysql -u root -p %1 < db\07_admin_services_init.sql

if %errorlevel% equ 0 (
    echo ✅ Databases created successfully
) else (
    echo ❌ Database initialization failed
    exit /b 1
)

REM 2. Build all modules
echo.
echo 🔨 Step 2: Building all modules...
call mvn clean install -DskipTests -q

if %errorlevel% equ 0 (
    echo ✅ Build completed successfully
) else (
    echo ❌ Build failed
    exit /b 1
)

REM 3. Summary
echo.
echo ==========================================
echo ✨ Setup Complete!
echo ==========================================
echo.
echo 📝 Next steps:
echo 1. Update application.properties in each service
echo 2. Run: java -jar BizFlow.AdminUserService\target\admin-user-service-*.jar
echo 3. Run: java -jar BizFlow.AdminProductService\target\admin-product-service-*.jar
echo 4. Run: java -jar BizFlow.AdminOrderService\target\admin-order-service-*.jar
echo 5. Run: java -jar BizFlow.AdminReportService\target\admin-report-service-*.jar
echo 6. Run: java -jar BizFlow.Gateway\target\gateway-*.jar
echo.
echo 🌐 Access:
echo    http://localhost:3000/admin/users.html
echo    http://localhost:3000/admin/products.html
echo    http://localhost:3000/admin/orders.html
echo    http://localhost:3000/admin/reports.html
echo.
echo ==========================================
pause

endlocal
