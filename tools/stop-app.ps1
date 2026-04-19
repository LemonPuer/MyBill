$marker = "org.lemon.mybill.just"

$processes = Get-CimInstance Win32_Process -Filter "name = 'java.exe' or name = 'javaw.exe'" |
    Where-Object {
        $_.CommandLine -and $_.CommandLine.IndexOf($marker, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
    }

if (-not $processes) {
    Write-Host "No MyBill Java process found."
    exit 0
}

foreach ($process in $processes) {
    Stop-Process -Id $process.ProcessId -Force
    Write-Host ("Stopped MyBill process PID {0}." -f $process.ProcessId)
}
