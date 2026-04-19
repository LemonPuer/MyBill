$mysql = Get-Command mysql -ErrorAction SilentlyContinue

if (-not $mysql) {
    Write-Error "mysql client was not found in PATH."
    exit 1
}

mysql -h 192.168.31.100 -P 3306 -u bill -pHDMEDpiAmPiaxrYi bill
exit $LASTEXITCODE
