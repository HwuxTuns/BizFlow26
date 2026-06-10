# PowerShell script to start BizFlow stack in Docker

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  BizFlow Stack Quick Starter (Windows)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check/copy .env file
$envFile = Join-Path $PSScriptRoot ".env"
$exampleFile = Join-Path $PSScriptRoot ".env.example"

if (-not (Test-Path $envFile)) {
    if (Test-Path $exampleFile) {
        Write-Host "[INFO] .env file not found. Copying from .env.example..." -ForegroundColor Yellow
        Copy-Item $exampleFile $envFile
        Write-Host "[OK] Created .env file successfully." -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Neither .env nor .env.example was found!" -ForegroundColor Red
        Exit 1
    }
} else {
    Write-Host "[OK] .env file exists." -ForegroundColor Green
}

# 2. Check if Docker is running
Write-Host "[DOCKER] Checking if Docker is running..." -ForegroundColor Gray
& docker info >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker is not running!" -ForegroundColor Red
    Write-Host "--> Please start Docker Desktop and try again." -ForegroundColor Yellow
    Write-Host ""
    Pause
    Exit 1
}
Write-Host "[OK] Docker is running." -ForegroundColor Green

# 3. Start the containers
Write-Host "[BUILD] Launching Docker Compose stack (build and start)..." -ForegroundColor Cyan
& docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Failed to start Docker Compose!" -ForegroundColor Red
    Write-Host ""
    Pause
    Exit 1
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "[SUCCESS] BizFlow is starting up!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access URLS:"
Write-Host "   - Frontend Website:   http://localhost:3000" -ForegroundColor Yellow
Write-Host "   - Database phpMyAdmin: http://localhost:8088" -ForegroundColor Yellow
Write-Host "   - Gateway API:         http://localhost:8000" -ForegroundColor Yellow
Write-Host ""
Write-Host "Note: Spring Boot microservices take about 45-60 seconds to fully initialize."
Write-Host "   Enjoy coding!" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""
