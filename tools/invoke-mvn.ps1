param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

$repoRoot = Split-Path $PSScriptRoot -Parent
Set-Location $repoRoot

$hasJava = $false

if ($env:JAVA_HOME) {
    $javac = Join-Path $env:JAVA_HOME "bin\javac.exe"
    if (Test-Path $javac) {
        $hasJava = $true
    }
}

if (-not $hasJava) {
    $fallbackJavaHome = "D:\Program\Java\java-21"
    $fallbackJavac = Join-Path $fallbackJavaHome "bin\javac.exe"

    if (Test-Path $fallbackJavac) {
        $env:JAVA_HOME = $fallbackJavaHome
        if (-not (($env:Path -split ';') -contains "$fallbackJavaHome\bin")) {
            $env:Path = "$fallbackJavaHome\bin;" + $env:Path
        }
        $hasJava = $true
    }
}

if (-not $hasJava -and -not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Error "No JDK was found. Configure JAVA_HOME or install a JDK, such as D:\Program\Java\java-21."
    exit 1
}

& mvn @Arguments
exit $LASTEXITCODE
