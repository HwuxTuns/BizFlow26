$ErrorActionPreference = "Stop"

$MYSQL_CONTAINER = "bizflow-mysql"
$MYSQL_USER = "root"
$MYSQL_PASSWORD = "123456"
$backupRoot = "db/backups/current"
$dbBackupDir = Join-Path $backupRoot "databases"
$imageBackupDir = Join-Path $backupRoot "product-images"

Write-Host "=== BizFlow Export Current Backup ===" -ForegroundColor Cyan

$running = docker ps --filter "name=$MYSQL_CONTAINER" --format "{{.Names}}"
if (-not $running) {
    Write-Host "MySQL container chua chay. Hay chay: docker compose up -d" -ForegroundColor Red
    exit 1
}

New-Item -ItemType Directory -Force -Path $dbBackupDir | Out-Null

$databases = @(docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD -N -e "SHOW DATABASES LIKE 'bizflow%';" 2>$null)
if (-not $databases) {
    Write-Host "Khong tim thay database bizflow*" -ForegroundColor Red
    exit 1
}

foreach ($db in $databases) {
    $outFile = Join-Path $dbBackupDir "$db.sql"
    Write-Host "Dumping $db -> $outFile"
    docker exec $MYSQL_CONTAINER mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD --databases $db --routines --triggers --events --single-transaction --default-character-set=utf8mb4 --set-charset --add-drop-database --add-drop-table --complete-insert --hex-blob --comments --dump-date > $outFile
    if ($LASTEXITCODE -ne 0) { throw "Dump that bai: $db" }
}

$combined = Join-Path $dbBackupDir "bizflow_all_databases.sql"
docker exec $MYSQL_CONTAINER mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD --databases $databases --routines --triggers --events --single-transaction --default-character-set=utf8mb4 --set-charset --add-drop-database --add-drop-table --complete-insert --hex-blob --comments --dump-date > $combined
if ($LASTEXITCODE -ne 0) { throw "Dump combined that bai" }

$sourceImageDir = "BizFlow.Frontend/assets/img/img_sanpham"
$sourceIndex = "BizFlow.Frontend/assets/data/product-image-files.json"
$destImageDir = Join-Path $imageBackupDir "img_sanpham"
$destDataDir = Join-Path $imageBackupDir "data"

New-Item -ItemType Directory -Force -Path $destImageDir, $destDataDir | Out-Null
if (Test-Path $sourceImageDir) {
    Copy-Item "$sourceImageDir/*" $destImageDir -Recurse -Force
}
if (Test-Path $sourceIndex) {
    Copy-Item $sourceIndex (Join-Path $destDataDir "product-image-files.json") -Force
}

$dbCount = (Get-ChildItem $dbBackupDir -Filter "bizflow_*.sql" -File | Measure-Object).Count
$imageCount = if (Test-Path $destImageDir) { (Get-ChildItem $destImageDir -File -Recurse | Measure-Object).Count } else { 0 }

Write-Host ""
Write-Host "Export hoan tat." -ForegroundColor Green
Write-Host "DB files: $dbCount"
Write-Host "Image files: $imageCount"
