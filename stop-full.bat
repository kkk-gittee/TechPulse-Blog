@echo off
chcp 65001 >nul
title TechPulse Blog - Docker Shutdown

echo.
echo ========================================
echo   Stopping TechPulse Blog (Docker)
echo ========================================
echo.

docker compose version >nul 2>&1
if %errorlevel% equ 0 (
    docker compose down
) else (
    docker-compose down 2>nul
)

echo.
echo All Docker services stopped.
echo.
pause
