$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$destJar = Join-Path $repoRoot 'gradle\wrapper\gradle-wrapper.jar'
$expectedHash = 'cb0da6751c2b753a16ac168bb354870ebb1e162e9083f116729cec9c781156b8'
if (-not (Test-Path $destJar)) {
    Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar' -OutFile $destJar -UseBasicParsing
}
if ((Get-FileHash $destJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) {
    throw 'Gradle wrapper checksum mismatch. Build stopped.'
}
Push-Location $repoRoot
try {
    & .\gradlew.bat testDebugUnitTest assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally { Pop-Location }
