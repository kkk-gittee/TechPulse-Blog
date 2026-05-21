@echo off
chcp 65001 >nul
title Blog System Shutdown
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    TechPulse Blog - Shutdown
echo ========================================
echo.

:: Stop Frontend (port 3000)
echo [1/3] Stopping frontend (port 3000^)...
set FOUND=0
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3000" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>nul
    set FOUND=1
)
if !FOUND! equ 1 (
    echo       Frontend stopped
) else (
    echo       Frontend not running
)
echo.

:: Stop Backend (port 8080)
echo [2/3] Stopping backend (port 8080^)...
set FOUND=0
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>nul
    set FOUND=1
)
if !FOUND! equ 1 (
    echo       Backend stopped
) else (
    echo       Backend not running
)
echo.

:: Stop Redis (port 6379)
echo [3/3] Stopping Redis (port 6379^)...
if exist "C:\Program Files\Redis\redis-cli.exe" (
    "C:\Program Files\Redis\redis-cli.exe" shutdown >nul 2>nul
)
timeout /t 2 >nul
set FOUND=0
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":6379" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>nul
    set FOUND=1
)
if !FOUND! equ 1 (
    echo       Redis stopped
) else (
    echo       Redis not running
)
echo.

echo ========================================
echo    All services stopped!
echo ========================================
echo.

endlocal
