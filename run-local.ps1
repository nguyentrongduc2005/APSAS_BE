<#
Run-local.ps1
Load environment variables from environment\api.txt into the current process
and start the Spring Boot app using the wrapper (mvnw.cmd).

Usage: In PowerShell at project root:
  powershell -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1

This script does NOT persist the variables to the system; they are set only for
the current process and the child mvnw process.
#>

Clear-Host
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $scriptDir

$envFile = Join-Path $scriptDir 'environment\api.txt'
if (-not (Test-Path $envFile)) {
    Write-Host "Could not find file: $envFile" -ForegroundColor Yellow
    exit 1
}

Write-Host "Loading environment variables from: $envFile" -ForegroundColor Cyan
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ([string]::IsNullOrWhiteSpace($line)) { return }
    if ($line.StartsWith('#')) { return }
    $pair = $line -split '=', 2
    if ($pair.Count -lt 2) { return }
    $name = $pair[0].Trim()
    $value = $pair[1].Trim()
    # Avoid printing secret values to console
    Write-Host "Setting env var: $name" -ForegroundColor Green
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
}

Write-Host "Starting Spring Boot (mvnw)..." -ForegroundColor Cyan
if (Test-Path .\mvnw.cmd) {
    & .\mvnw.cmd spring-boot:run
} else {
    Write-Host "mvnw.cmd not found in project root; attempt to run 'mvn spring-boot:run' instead" -ForegroundColor Yellow
    & mvn spring-boot:run
}
 # chạy dự án
# powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1