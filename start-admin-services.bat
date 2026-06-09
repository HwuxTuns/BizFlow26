@echo off
REM Start all Admin Services for BizFlow
REM Make sure MySQL is running in Docker first

setlocal

REM Prefer repo-local JDK (team-friendly) if present
set "REPO_JDK=%~dp0.jdk\jdk-21.0.8"
if exist "%REPO_JDK%\bin\java.exe" (
    set "JAVA_HOME=%REPO_JDK%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo ============================================
echo  STARTING BIZFLOW ADMIN SERVICES
echo ============================================
echo.

REM Check if MySQL is running
docker ps | findstr bizflow-mysql >nul
if errorlevel 1 (
    echo ERROR: MySQL container is not running!
    echo Please start Docker containers first with: docker-compose up -d
    pause
    exit /b 1
)

echo ✓ MySQL is running
echo.

REM Kill any existing Java processes on admin ports
echo Stopping existing admin services...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8200') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8201') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8202') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8203') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8204') do taskkill /F /PID %%a 2>nul
timeout /t 2 /nobreak >nul

echo.
echo Starting Admin Services...
echo.

REM Start AdminHomeService (Port 8200)
echo [1/5] Starting AdminHomeService on port 8200...
start "AdminHomeService" cmd /k "cd BizFlow.AdminHomeService && java -jar target\admin-home-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

REM Start AdminUserService (Port 8201)
echo [2/5] Starting AdminUserService on port 8201...
start "AdminUserService" cmd /k "cd BizFlow.AdminUserService && java -jar target\admin-user-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

REM Start AdminProductService (Port 8202)
echo [3/5] Starting AdminProductService on port 8202...
start "AdminProductService" cmd /k "cd BizFlow.AdminProductService && java -jar target\admin-product-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

REM Start AdminOrderService (Port 8203)
echo [4/5] Starting AdminOrderService on port 8203...
start "AdminOrderService" cmd /k "cd BizFlow.AdminOrderService && java -jar target\admin-order-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

REM Start AdminReportService (Port 8204)
echo [5/5] Starting AdminReportService on port 8204...
start "AdminReportService" cmd /k "cd BizFlow.AdminReportService && java -jar target\admin-report-service-0.0.1-SNAPSHOT.jar"
timeout /t 3 /nobreak >nul

echo.
echo ============================================
echo  ALL ADMIN SERVICES STARTED!
echo ============================================
echo.
echo Services running on:
echo   - AdminHomeService:    http://localhost:8200
echo   - AdminUserService:    http://localhost:8201
echo   - AdminProductService: http://localhost:8202
echo   - AdminOrderService:   http://localhost:8203
echo   - AdminReportService:  http://localhost:8204
echo.
echo Press any key to check service status...
pause >nul

REM Check if all services are running
echo.
echo Checking service status...
netstat -ano | findstr ":8200 :8201 :8202 :8203 :8204" | findstr "LISTENING"

echo.
echo Press any key to exit...
pause >nul

endlocal
