@echo off
chcp 65001 >nul
title Blog System Startup
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    TechPulse Blog - Startup
echo ========================================
echo.

set PROJECT_DIR=%~dp0
set BACKEND_DIR=%PROJECT_DIR%..\blog-backend

:: Check prerequisites
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found! Please install Node.js 18+
    pause
    exit /b 1
)

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Please install JDK 17+
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found! Please install Maven 3.6+
    pause
    exit /b 1
)

:: Check if services are already running
set ALREADY_RUNNING=0

netstat -ano 2>nul | findstr ":8080.*LISTENING" >nul 2>nul
if %errorlevel% equ 0 (
    echo [WARN] Port 8080 already in use
    set ALREADY_RUNNING=1
)

netstat -ano 2>nul | findstr ":3000.*LISTENING" >nul 2>nul
if %errorlevel% equ 0 (
    echo [WARN] Port 3000 already in use
    set ALREADY_RUNNING=1
)

if !ALREADY_RUNNING! equ 1 (
    echo.
    echo Services already running. Press Y to restart, any other key to exit.
    choice /c YN /n /m "Continue? "
    if !errorlevel! neq 1 (
        echo Aborted.
        exit /b 0
    )
    echo.
    echo Stopping existing services...
    for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>nul
    for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3000" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>nul
    timeout /t 2 >nul
    echo Existing services stopped.
    echo.
)

:: Start MySQL
echo [1/5] Starting MySQL...
netstat -ano 2>nul | findstr ":3306.*LISTENING" >nul 2>nul
if %errorlevel% equ 0 (
    echo       MySQL already running
) else (
    sc query MySQL80 2>nul | findstr "STOPPED" >nul 2>nul
    if !errorlevel! equ 0 (
        echo       Starting MySQL80 service...
        net start MySQL80 >nul 2>nul
        if !errorlevel! neq 0 (
            echo       [WARN] Failed to start MySQL (may need admin rights)
            echo       Please start MySQL manually
        ) else (
            timeout /t 3 >nul
            echo       MySQL started
        )
    ) else (
        echo       MySQL80 service not found, skipping
    )
)
echo.

:: Start Redis
echo [2/5] Starting Redis...
netstat -ano 2>nul | findstr ":6379.*LISTENING" >nul 2>nul
if %errorlevel% equ 0 (
    echo       Redis already running
) else (
    if exist "C:\Program Files\Redis\redis-server.exe" (
        start "Redis" "C:\Program Files\Redis\redis-server.exe"
        timeout /t 2 >nul
        echo       Redis started
    ) else (
        echo       Redis not found, skipping
    )
)
echo.

:: Build Backend
echo [3/5] Building backend...
set JAR_PATH=%BACKEND_DIR%\target\blog-backend-1.0.0.jar

set NEED_BUILD=1
if exist "%JAR_PATH%" (
    for %%F in ("%BACKEND_DIR%\pom.xml") do (
        for %%J in ("%JAR_PATH%") do (
            if %%~tF LEQ %%~tJ set NEED_BUILD=0
        )
    )
)

if !NEED_BUILD! equ 1 (
    echo       Building with Maven...
    cd /d "%BACKEND_DIR%"
    call mvn package -DskipTests -q 2>nul
    if !errorlevel! neq 0 (
        echo [ERROR] Maven build failed!
        pause
        exit /b 1
    )
    cd /d "%PROJECT_DIR%"
    echo       Build successful!
) else (
    echo       JAR up-to-date, skipping build
)
echo.

:: Start Backend
echo [4/5] Starting backend (port 8080^)...
if not exist "%JAR_PATH%" (
    echo [ERROR] JAR not found: %JAR_PATH%
    pause
    exit /b 1
)
start "Blog-Backend" cmd /c "cd /d "%BACKEND_DIR%" && java -jar target\blog-backend-1.0.0.jar"
echo       Backend starting...
echo.

:: Start Frontend
echo [5/5] Starting frontend (port 3000^)...
if not exist "%PROJECT_DIR%node_modules" (
    echo       Installing frontend dependencies...
    cd /d "%PROJECT_DIR%"
    call npm install --cache C:/Users/K1931/AppData/Local/Temp/npm-cache 2>nul
    cd /d "%PROJECT_DIR%"
)
start "Blog-Frontend" cmd /c "cd /d "%PROJECT_DIR%" && npx vite --host"
echo       Frontend starting...
echo.

:: Health check
echo Waiting for services...
set BACKEND_OK=0
set FRONTEND_OK=0

for /L %%i in (1,1,30) do (
    if !BACKEND_OK! equ 0 (
        netstat -ano 2>nul | findstr ":8080.*LISTENING" >nul 2>nul
        if !errorlevel! equ 0 set BACKEND_OK=1
    )
    if !FRONTEND_OK! equ 0 (
        netstat -ano 2>nul | findstr ":3000.*LISTENING" >nul 2>nul
        if !errorlevel! equ 0 set FRONTEND_OK=1
    )
    if !BACKEND_OK! equ 1 if !FRONTEND_OK! equ 1 goto :ready
    timeout /t 1 >nul
)

:ready
echo.
echo ========================================
if !BACKEND_OK! equ 1 (
    echo    Backend:   http://localhost:8080  [OK]
) else (
    echo    Backend:   http://localhost:8080  [TIMEOUT]
)
if !FRONTEND_OK! equ 1 (
    echo    Frontend:  http://localhost:3000  [OK]
) else (
    echo    Frontend:  http://localhost:3000  [TIMEOUT]
)
echo    API Docs:  http://localhost:8080/doc.html
echo ========================================
echo.

start http://localhost:3000

endlocal
