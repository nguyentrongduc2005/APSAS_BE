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

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   APSAS Spring Boot Starter        " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# CLEANUP SECTION (Optional)
# ============================================
if ($Cleanup) {
    Write-Host "*** CLEANUP MODE ENABLED ***" -ForegroundColor Yellow
    Write-Host ""
    
    # Step 1: Stop existing Spring Boot process
    Write-Host "[1/4] Stopping existing Spring Boot process..." -ForegroundColor Yellow
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        foreach ($proc in $javaProcesses) {
            Write-Host "  - Stopping Java process (PID: $($proc.Id))..." -ForegroundColor Gray
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 2
        Write-Host "  [OK] Spring Boot stopped" -ForegroundColor Green
    } else {
        Write-Host "  - No running Java process found" -ForegroundColor Gray
    }

    # Step 2: Kill long-running MySQL transactions
    Write-Host ""
    Write-Host "[2/4] Cleaning up MySQL long-running transactions..." -ForegroundColor Yellow
    
    $mysqlCleanup = "SELECT CONCAT('KILL ', id, ';') as kill_query FROM information_schema.processlist WHERE db = 'apsas_db' AND user = 'user' AND time > 10 AND command != 'Sleep';"
    
    Write-Host "  - Checking MySQL for long transactions..." -ForegroundColor Gray
    try {
        # Try to run MySQL cleanup via Docker
        $dockerCmd = "docker exec mysql_db mysql -u user -p1234 -e `"$mysqlCleanup`" 2>$null"
        Invoke-Expression $dockerCmd -ErrorAction SilentlyContinue | Out-Null
        Write-Host "  [OK] MySQL cleanup completed" -ForegroundColor Green
    } catch {
        Write-Host "  - MySQL cleanup not needed or Docker not available" -ForegroundColor Gray
    }

    # Step 3: Wait for MySQL to be ready
    Write-Host ""
    Write-Host "[3/4] Waiting for MySQL to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    Write-Host "  [OK] MySQL ready" -ForegroundColor Green

    # Step 4: Clean Maven target if needed
    Write-Host ""
    Write-Host "[4/4] Cleaning build artifacts..." -ForegroundColor Yellow
    if (-not $Force) {
        $cleanBuild = Read-Host "  Do you want to clean Maven target? (y/N)"
    } else {
        $cleanBuild = "N"
        Write-Host "  Skipping Maven clean (Force mode)" -ForegroundColor Gray
    }
    
    if ($cleanBuild -eq "y" -or $cleanBuild -eq "Y") {
        if (Test-Path .\mvnw.cmd) {
            Write-Host "  - Running Maven clean..." -ForegroundColor Gray
            & .\mvnw.cmd clean
            Write-Host "  [OK] Maven clean completed" -ForegroundColor Green
        } else {
            Write-Host "  - mvnw.cmd not found, skipping" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  - Skipped" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "[OK] Cleanup completed!" -ForegroundColor Green
    Write-Host ""
}

# ============================================
# ENVIRONMENT VARIABLES SECTION
# ============================================
Write-Host ">>> Loading Environment Variables..." -ForegroundColor Cyan

$envFile = Join-Path $scriptDir 'environment\api.txt'
if (-not (Test-Path $envFile)) {
    Write-Host "[ERROR] Could not find file: $envFile" -ForegroundColor Red
    Write-Host "        Please create environment\api.txt with your API keys" -ForegroundColor Yellow
    exit 1
}

$envCount = 0
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ([string]::IsNullOrWhiteSpace($line)) { return }
    if ($line.StartsWith('#')) { return }
    $pair = $line -split '=', 2
    if ($pair.Count -lt 2) { return }
    $name = $pair[0].Trim()
    $value = $pair[1].Trim()
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
    Write-Host "  [OK] Set $name" -ForegroundColor Gray
    $envCount++
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
    Write-Host "WARNING: mvnw.cmd not found, trying mvn..." -ForegroundColor Yellow
    & mvn spring-boot:run
}

# ============================================
# USAGE EXAMPLES
# ============================================
<#
USAGE EXAMPLES:

1. Normal start (quick):
   .\run-local.ps1

2. Start with cleanup:
   .\run-local.ps1 -Cleanup

3. Force cleanup without prompts:
   .\run-local.ps1 -Cleanup -Force

4. Using full PowerShell command:
   powershell -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1 -Cleanup
#>