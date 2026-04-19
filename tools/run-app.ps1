param(
    [string]$Profile = "dev"
)

$repoRoot = Split-Path $PSScriptRoot -Parent
$invokeMvn = Join-Path $PSScriptRoot "invoke-mvn.ps1"
$writeJavaPid = Join-Path $PSScriptRoot "write-java-pid.ps1"
$marker = "-Dmybill.just.marker=org.lemon.mybill.just"
$targetDir = Join-Path $repoRoot "target"
$stdoutLog = Join-Path $targetDir "just-run.out.log"
$stderrLog = Join-Path $targetDir "just-run.err.log"
$pidFile = Join-Path $targetDir "just-run-java.pid"

New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

if (Test-Path $stdoutLog) {
    Remove-Item $stdoutLog -Force
}

if (Test-Path $stderrLog) {
    Remove-Item $stderrLog -Force
}

if (Test-Path $pidFile) {
    Remove-Item $pidFile -Force
}

$process = Start-Process -FilePath "powershell.exe" -ArgumentList @(
    "-NoLogo",
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-Command",
    "Set-Location '$repoRoot'; Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoLogo','-NoProfile','-ExecutionPolicy','Bypass','-File','$writeJavaPid','$pidFile') -WindowStyle Hidden | Out-Null; & '$invokeMvn' 'spring-boot:run' '-Dspring-boot.run.profiles=$Profile' '-Dspring-boot.run.jvmArguments=$marker'"
) -WorkingDirectory $repoRoot -WindowStyle Hidden -RedirectStandardOutput $stdoutLog -RedirectStandardError $stderrLog -PassThru

$deadline = (Get-Date).AddSeconds(3)

do {
    if (Test-Path $pidFile) {
        $javaPid = (Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1).Trim()
        if ($javaPid -match '^\d+$') {
            Write-Host ("PID: {0}" -f $javaPid)
            Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
            Write-Host "MyBill started"
            exit 0
        }
    }

    if ($process.HasExited) {
        Write-Host ("PID: {0}" -f $process.Id)
        Write-Host "MyBill started"
        exit 0
    }

    Start-Sleep -Milliseconds 250
} while ((Get-Date) -lt $deadline)

Write-Host ("PID: {0}" -f $process.Id)
Write-Host "MyBill starting"
