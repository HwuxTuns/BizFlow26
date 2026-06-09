$ErrorActionPreference = "Stop"

$MYSQL_CONTAINER = "bizflow-mysql"
$MYSQL_USER = "root"
$MYSQL_PASSWORD = "123456"
$backupRoot = "db/backups/current"
$dbBackupDir = Join-Path $backupRoot "databases"
$imageBackupDir = Join-Path $backupRoot "product-images"

Write-Host "=== BizFlow Restore Current Backup ===" -ForegroundColor Cyan

$running = docker ps --filter "name=$MYSQL_CONTAINER" --format "{{.Names}}"
if (-not $running) {
    Write-Host "MySQL container chua chay. Hay chay: docker compose up -d" -ForegroundColor Red
    exit 1
}

$dbFiles = Get-ChildItem -Path $dbBackupDir -Filter "bizflow_*.sql" -File |
    Where-Object { $_.Name -ne "bizflow_all_databases.sql" } |
    Sort-Object Name

if (-not $dbFiles) {
    Write-Host "Khong tim thay file SQL backup trong $dbBackupDir" -ForegroundColor Red
    exit 1
}

Write-Host "Dang import database..." -ForegroundColor Yellow
foreach ($file in $dbFiles) {
    Write-Host "  -> $($file.Name)"
    docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD < $file.FullName
    if ($LASTEXITCODE -ne 0) {
        throw "Import that bai: $($file.FullName)"
    }
}

$destImageDir = "BizFlow.Frontend/assets/img/img_sanpham"
$destDataDir = "BizFlow.Frontend/assets/data"
New-Item -ItemType Directory -Force -Path $destImageDir, $destDataDir | Out-Null

if (Test-Path (Join-Path $imageBackupDir "img_sanpham")) {
    Copy-Item -Path (Join-Path $imageBackupDir "img_sanpham/*") -Destination $destImageDir -Recurse -Force
}
if (Test-Path (Join-Path $imageBackupDir "data/product-image-files.json")) {
    Copy-Item -Path (Join-Path $imageBackupDir "data/product-image-files.json") -Destination (Join-Path $destDataDir "product-image-files.json") -Force
}

Write-Host "" 
Write-Host "Restore hoan tat." -ForegroundColor Green
Write-Host "Kiem tra DB:" -ForegroundColor Cyan
docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW DATABASES LIKE 'bizflow%';"
