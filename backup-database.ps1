# BizFlow Database Backup Script
# Tạo backup toàn bộ cơ sở dữ liệu MySQL

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "db/bizflow_full_backup_$timestamp.sql"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "BizFlow Database Backup" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra container MySQL có đang chạy không
$containerRunning = docker ps --filter "name=bizflow-mysql" --format "{{.Names}}"
if (-not $containerRunning) {
    Write-Host "ERROR: Container bizflow-mysql khong chay!" -ForegroundColor Red
    Write-Host "Vui long chay: docker compose up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Container MySQL dang chay" -ForegroundColor Green
Write-Host ""

# Thông tin kết nối
$MYSQL_ROOT_PASSWORD = "123456"
$MYSQL_HOST = "mysql"
$MYSQL_USER = "root"

Write-Host "Dang tao backup toan bo database..." -ForegroundColor Yellow
Write-Host "File backup: $backupFile" -ForegroundColor Cyan
Write-Host ""

# Tạo backup với tất cả databases
docker exec bizflow-mysql mysqldump `
    -uroot `
    -p123456 `
    --all-databases `
    --routines `
    --triggers `
    --events `
    --single-transaction `
    --default-character-set=utf8mb4 `
    --set-charset `
    --add-drop-database `
    --add-drop-table `
    --complete-insert `
    --hex-blob `
    --comments `
    --dump-date `
    > $backupFile

if ($LASTEXITCODE -eq 0) {
    $fileSize = (Get-Item $backupFile).Length / 1MB
    Write-Host ""
    Write-Host "==================================" -ForegroundColor Green
    Write-Host "Backup thanh cong!" -ForegroundColor Green
    Write-Host "==================================" -ForegroundColor Green
    Write-Host "File: $backupFile" -ForegroundColor Cyan
    Write-Host "Kich thuoc: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Cyan
    Write-Host ""
    
    # Hien thi danh sach cac database duoc backup
    Write-Host "Cac database da backup:" -ForegroundColor Yellow
    docker exec bizflow-mysql mysql -uroot -p123456 -e "SHOW DATABASES;" 2>$null | Select-Object -Skip 1
    
    Write-Host ""
    Write-Host "De restore backup nay, chay:" -ForegroundColor Yellow
    Write-Host "docker exec -i bizflow-mysql mysql -uroot -p123456 < $backupFile" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "ERROR: Backup thất bại!" -ForegroundColor Red
    exit 1
}
