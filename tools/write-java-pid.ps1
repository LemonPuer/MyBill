param(
    [Parameter(Mandatory = $true)]
    [string]$PidFile
)

$deadline = (Get-Date).AddSeconds(15)

do {
    $javaProcess = Get-CimInstance Win32_Process -Filter "name = 'java.exe' or name = 'javaw.exe'" |
        Where-Object {
            $_.CommandLine -and
            $_.CommandLine.IndexOf("org.lemon.mybill.just", [System.StringComparison]::OrdinalIgnoreCase) -ge 0 -and
            $_.CommandLine.IndexOf("org.lemon.App", [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        } |
        Sort-Object ProcessId -Descending |
        Select-Object -First 1

    if ($javaProcess) {
        Set-Content -Path $PidFile -Value $javaProcess.ProcessId -NoNewline
        exit 0
    }

    Start-Sleep -Milliseconds 250
} while ((Get-Date) -lt $deadline)
