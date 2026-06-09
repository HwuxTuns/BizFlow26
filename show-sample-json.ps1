param(
  [string]$BaseUrl = "http://127.0.0.1:3000",
  [int]$OrdersTop = 2,
  [int]$ReportsTop = 5
)

$ErrorActionPreference = 'Stop'

function Get-Utf8Json {
  param([Parameter(Mandatory=$true)][string]$Url)

  $r = Invoke-WebRequest -UseBasicParsing $Url
  $ms = New-Object System.IO.MemoryStream
  $r.RawContentStream.CopyTo($ms)
  $bytes = $ms.ToArray()

  # Windows PowerShell 5.x can mis-decode JSON when Content-Type has no charset.
  # Decode explicitly as UTF-8.
  $text = [Text.Encoding]::UTF8.GetString($bytes)
  return $text | ConvertFrom-Json
}

Write-Host "BaseUrl: $BaseUrl" -ForegroundColor Cyan

$ordersUrl = "$BaseUrl/api/admin/orders"
$reportsUrl = "$BaseUrl/api/admin/reports"

Write-Host "" 
Write-Host "GET $ordersUrl (first $OrdersTop)" -ForegroundColor Cyan
$orders = Get-Utf8Json -Url $ordersUrl
$orders | Select-Object -First $OrdersTop | ConvertTo-Json -Depth 10

Write-Host "" 
Write-Host "GET $reportsUrl (first $ReportsTop)" -ForegroundColor Cyan
$reports = Get-Utf8Json -Url $reportsUrl
$reports | Select-Object -First $ReportsTop | ConvertTo-Json -Depth 10
