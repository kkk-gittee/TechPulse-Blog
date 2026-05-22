@echo off
chcp 65001 >nul
title TechPulse Blog - Docker Full Stack
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   TechPulse Blog - Docker Full Stack
echo ========================================
echo.

:: Step 1: Check prerequisites
echo [1/4] Checking prerequisites...

docker -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed.
    pause
    exit /b 1
)

docker compose version >nul 2>&1
if %errorlevel% neq 0 (
    docker-compose -v >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERROR] Docker Compose is not installed.
        pause
        exit /b 1
    )
    set COMPOSE_CMD=docker-compose
) else (
    set COMPOSE_CMD=docker compose
)

node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed.
    pause
    exit /b 1
)

echo [OK] All prerequisites found.
echo.

:: Step 2: Start Docker infrastructure
echo [2/4] Starting Docker infrastructure (MySQL + Redis + RabbitMQ + Backend)...
%COMPOSE_CMD% up -d
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose failed to start.
    pause
    exit /b 1
)
echo.

:: Step 3: Wait for backend health
echo [3/4] Waiting for backend to become healthy...
set /a elapsed=0

:wait_loop
if %elapsed% geq 120 (
    echo [WARN] Backend did not become healthy within 120s. Continuing anyway...
    goto after_wait
)
curl -sf http://localhost:8080/api/category/list >nul 2>&1
if %errorlevel% equ 0 (
    echo   Backend API: OK (%elapsed%s^)
    goto after_wait
)
timeout /t 2 /nobreak >nul
set /a elapsed+=2
echo   Waiting... (%elapsed%s^)
goto wait_loop

:after_wait
echo.

:: Step 4: Start frontend dev server
echo [4/4] Starting frontend dev server...
cd /d "%~dp0blog-frontend"

if not exist node_modules (
    echo   Installing frontend dependencies...
    call npm install --cache C:/Users/K1931/AppData/Local/Temp/npm-cache 2>nul
)

echo.
echo ========================================
echo   All services are running!
echo ========================================
echo   Frontend:       http://localhost:3000
echo   Backend API:    http://localhost:8080
echo   API Docs:       http://localhost:8080/doc.html
echo   RabbitMQ Mgmt:  http://localhost:15672 (guest/guest)
echo.
echo   Press Ctrl+C to stop the frontend.
echo   Run stop-full.bat to stop Docker services.
echo ========================================
echo.

call npm run dev

pause
