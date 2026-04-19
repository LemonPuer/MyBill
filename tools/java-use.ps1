param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Version
)

switch ($Version) {
    "8" {
        $javaHome = "D:\Program\Java\java-1.8"
    }
    "21" {
        $javaHome = "D:\Program\Java\java-21"
    }
    default {
        Write-Error "Unsupported Java version '$Version'. Supported versions: 8, 21."
        exit 1
    }
}

$javaBin = Join-Path $javaHome "bin"

if (-not (Test-Path (Join-Path $javaBin "java.exe"))) {
    Write-Error "Configured Java path does not exist or is incomplete: $javaHome"
    exit 1
}

function Test-IsAdministrator {
    $currentIdentity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentIdentity)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Normalize-PathEntry {
    param([string]$Entry)

    if ([string]::IsNullOrWhiteSpace($Entry)) {
        return $null
    }

    return $Entry.Trim().TrimEnd('\\')
}

if (-not (Test-IsAdministrator)) {
    try {
        $process = Start-Process -FilePath "powershell.exe" -Verb RunAs -ArgumentList @(
            "-NoLogo",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            $PSCommandPath,
            $Version
        ) -Wait -PassThru
        exit $process.ExitCode
    }
    catch {
        Write-Error "Administrator elevation was cancelled or failed."
        exit 1
    }
}

$javaBinPattern = '^D:\\Program\\Java\\[^\\]+\\bin$'
$oracleJavaPath = 'C:\Program Files (x86)\Common Files\Oracle\Java\javapath'
$machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$pathEntries = @()

if (-not [string]::IsNullOrWhiteSpace($machinePath)) {
    $pathEntries = $machinePath -split ';'
}

$selectedNormalized = Normalize-PathEntry $javaBin
$oracleJavaPathNormalized = Normalize-PathEntry $oracleJavaPath
$preservedEntries = New-Object System.Collections.Generic.List[string]
$oracleIndex = -1

foreach ($entry in $pathEntries) {
    $normalized = Normalize-PathEntry $entry

    if (-not $normalized) {
        continue
    }

    if ($normalized -match $javaBinPattern) {
        continue
    }

    if ($oracleIndex -lt 0 -and $normalized.ToLowerInvariant() -eq $oracleJavaPathNormalized.ToLowerInvariant()) {
        $oracleIndex = $preservedEntries.Count
    }

    $preservedEntries.Add($entry.Trim()) | Out-Null
}

$updatedPathEntries = New-Object System.Collections.Generic.List[string]
for ($index = 0; $index -lt $preservedEntries.Count; $index++) {
    if ($index -eq $oracleIndex) {
        $updatedPathEntries.Add($selectedNormalized) | Out-Null
    }

    $updatedPathEntries.Add($preservedEntries[$index]) | Out-Null
}

if ($oracleIndex -lt 0) {
    $updatedPathEntries.Insert(0, $selectedNormalized)
}

[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
[Environment]::SetEnvironmentVariable("Path", ($updatedPathEntries -join ';'), "Machine")

$env:JAVA_HOME = $javaHome
$currentEntries = @()

if (-not [string]::IsNullOrWhiteSpace($env:Path)) {
    $currentEntries = $env:Path -split ';' | Where-Object {
        $normalized = Normalize-PathEntry $_
        $normalized -and $normalized -notmatch $javaBinPattern
    }
}

$env:Path = (@($selectedNormalized) + $currentEntries) -join ';'

Write-Host "Set machine-level JAVA_HOME to $javaHome"
Write-Host "Updated machine-level Path to use $selectedNormalized"
Write-Host "Open a new terminal to pick up the updated machine environment."
