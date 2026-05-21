@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo        Blog Backend 启动脚本
echo ========================================
echo.

:: 检查 Java 环境
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java 环境，请先安装 JDK 1.8+
    pause
    exit /b 1
)

:: 检查 Maven 环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven 环境，请先安装 Maven 3.6+
    pause
    exit /b 1
)

echo [信息] 检测到 Java 和 Maven 环境
echo.

:: 选择启动模式
echo 请选择启动模式:
echo   1. Docker 启动 (推荐，需要安装 Docker)
echo   2. 本地开发启动 (需要本地 MySQL、Redis、RabbitMQ)
echo   3. 仅启动后端 (使用已有数据库)
echo.
set /p choice="请输入选择 (1/2/3): "

if "%choice%"=="1" (
    goto docker_start
) else if "%choice%"=="2" (
    goto local_start
) else if "%choice%"=="3" (
    goto app_start
) else (
    echo [错误] 无效的选择
    pause
    exit /b 1
)

:docker_start
echo.
echo [信息] 使用 Docker 启动所有服务...
echo.

:: 检查 Docker
docker -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Docker 环境，请先安装 Docker Desktop
    pause
    exit /b 1
)

:: 检查 docker-compose
docker-compose -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 docker-compose，请先安装
    pause
    exit /b 1
)

echo [信息] 正在启动 Docker 服务...
docker-compose up -d

echo.
echo [信息] 等待服务启动...
timeout /t 30 /nobreak >nul

echo.
echo ========================================
echo   服务启动完成！
echo   - 应用地址: http://localhost:8080
echo   - 接口文档: http://localhost:8080/doc.html
echo   - RabbitMQ管理: http://localhost:15672 (guest/guest)
echo ========================================
echo.
pause
exit /b 0

:local_start
echo.
echo [信息] 使用本地环境启动...
echo [提示] 请确保本地已启动 MySQL、Redis、RabbitMQ
echo.

:: 检查 MySQL
netstat -an | findstr ":3306" >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到 MySQL 服务，请确保 MySQL 已启动
)

:: 检查 Redis
netstat -an | findstr ":6379" >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到 Redis 服务，请确保 Redis 已启动
)

:: 检查 RabbitMQ
netstat -an | findstr ":5672" >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到 RabbitMQ 服务，请确保 RabbitMQ 已启动
)

echo.
goto app_start

:app_start
echo.
echo [信息] 正在编译项目...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo [错误] 项目编译失败
    pause
    exit /b 1
)

echo.
echo [信息] 正在启动应用...
echo.

:: 查找 jar 文件
for /f "tokens=*" %%i in ('dir /b /s target\*.jar 2^>nul ^| findstr /v "original"') do (
    set JAR_FILE=%%i
)

if not defined JAR_FILE (
    echo [错误] 未找到可执行的 jar 文件
    pause
    exit /b 1
)

echo [信息] 启动应用: !JAR_FILE!
echo.
echo ========================================
echo   应用启动中...
echo   - 应用地址: http://localhost:8080
echo   - 接口文档: http://localhost:8080/doc.html
echo   按 Ctrl+C 停止应用
echo ========================================
echo.

java -jar !JAR_FILE!

pause
