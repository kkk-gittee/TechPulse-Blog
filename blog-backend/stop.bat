@echo off
chcp 65001 >nul

echo ========================================
echo        Blog Backend 停止脚本
echo ========================================
echo.

:: 选择停止模式
echo 请选择停止模式:
echo   1. 停止 Docker 服务
echo   2. 停止本地 Java 进程
echo   3. 停止所有服务
echo.
set /p choice="请输入选择 (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo [信息] 正在停止 Docker 服务...
    docker-compose down
    echo [信息] Docker 服务已停止
) else if "%choice%"=="2" (
    echo.
    echo [信息] 正在查找并停止 Java 进程...
    tasklist /FI "IMAGENAME eq java.exe" 2>nul | findstr /I "java.exe" >nul
    if not errorlevel 1 (
        taskkill /F /IM java.exe
        echo [信息] Java 进程已停止
    ) else (
        echo [信息] 未找到运行中的 Java 进程
    )
) else if "%choice%"=="3" (
    echo.
    echo [信息] 正在停止 Docker 服务...
    docker-compose down 2>nul
    echo.
    echo [信息] 正在停止 Java 进程...
    taskkill /F /IM java.exe 2>nul
    echo.
    echo [信息] 所有服务已停止
) else (
    echo [错误] 无效的选择
)

echo.
pause
