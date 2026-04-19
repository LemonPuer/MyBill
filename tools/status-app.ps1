$marker = "org.lemon.mybill.just"

$processes = Get-CimInstance Win32_Process -Filter "name = 'java.exe' or name = 'javaw.exe'" |
    Where-Object {
        $_.CommandLine -and $_.CommandLine.IndexOf($marker, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
    }

if (-not $processes) {
    Write-Host "MyBill is not running."
    exit 0
}

$processes | Select-Object ProcessId, Name, CommandLine | Format-List
