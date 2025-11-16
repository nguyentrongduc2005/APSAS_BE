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

# Check if Docker is running
Write-Host "`n==> Checking Docker..." -ForegroundColor Cyan
$dockerRunning = docker ps 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running or not installed!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop first." -ForegroundColor Yellow
    exit 1
}

# Start Docker Compose services
Write-Host "`n==> Starting Docker Compose services..." -ForegroundColor Cyan
$dockerComposePath = Join-Path $scriptDir 'environment\docker-compose.yml'
if (Test-Path $dockerComposePath) {
    Set-Location (Join-Path $scriptDir 'environment')
    docker compose up -d
    Set-Location $scriptDir
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker services started successfully!" -ForegroundColor Green
        Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
        Start-Sleep -Seconds 15
    } else {
        Write-Host "WARNING: Failed to start docker compose services." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "WARNING: docker-compose.yml not found at: $dockerComposePath" -ForegroundColor Yellow
}

# Clean and build project
Write-Host "`n==> Cleaning and building project..." -ForegroundColor Cyan
if (Test-Path .\mvnw.cmd) {
    Write-Host "Running: mvnw clean compile" -ForegroundColor Yellow
    & .\mvnw.cmd clean compile
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Build completed successfully!" -ForegroundColor Green
} else {
    Write-Host "WARNING: mvnw.cmd not found, skipping clean build" -ForegroundColor Yellow
}

Write-Host "`n==> Starting Spring Boot application..." -ForegroundColor Cyan
if (Test-Path .\mvnw.cmd) {
    & .\mvnw.cmd spring-boot:run
} else {
    Write-Host "mvnw.cmd not found in project root; attempt to run 'mvn spring-boot:run' instead" -ForegroundColor Yellow
    & mvn spring-boot:run
}
 # chạy dự án
# powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1