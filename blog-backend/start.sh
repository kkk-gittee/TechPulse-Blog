#!/bin/bash

echo "========================================"
echo "       Blog Backend 启动脚本"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo -e "${RED}[错误] 未检测到 Java 环境，请先安装 JDK 17+${NC}"
    exit 1
fi

# 检查 Maven 环境
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[错误] 未检测到 Maven 环境，请先安装 Maven 3.6+${NC}"
    exit 1
fi

echo -e "${GREEN}[信息] 检测到 Java 和 Maven 环境${NC}"
echo ""

# ========== 函数定义（必须在调用前） ==========

docker_start() {
    echo ""
    echo -e "${GREEN}[信息] 使用 Docker 启动所有服务...${NC}"
    echo ""

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[错误] 未检测到 Docker 环境，请先安装 Docker${NC}"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}[错误] 未检测到 docker-compose，请先安装${NC}"
        exit 1
    fi

    echo -e "${GREEN}[信息] 正在启动 Docker 服务...${NC}"
    docker-compose up -d

    echo ""
    echo -e "${GREEN}[信息] 等待服务启动...${NC}"
    sleep 30

    echo ""
    echo "========================================"
    echo "  服务启动完成！"
    echo "  - 应用地址: http://localhost:8080"
    echo "  - 接口文档: http://localhost:8080/doc.html"
    echo "  - RabbitMQ管理: http://localhost:15672 (guest/guest)"
    echo "========================================"
    echo ""
}

local_start() {
    echo ""
    echo -e "${GREEN}[信息] 使用本地环境启动...${NC}"
    echo -e "${YELLOW}[提示] 请确保本地已启动 MySQL、Redis、RabbitMQ${NC}"
    echo ""

    if ! nc -z localhost 3306 2>/dev/null; then
        echo -e "${YELLOW}[警告] 未检测到 MySQL 服务，请确保 MySQL 已启动${NC}"
    fi

    if ! nc -z localhost 6379 2>/dev/null; then
        echo -e "${YELLOW}[警告] 未检测到 Redis 服务，请确保 Redis 已启动${NC}"
    fi

    if ! nc -z localhost 5672 2>/dev/null; then
        echo -e "${YELLOW}[警告] 未检测到 RabbitMQ 服务，请确保 RabbitMQ 已启动${NC}"
    fi

    echo ""
    app_start
}

app_start() {
    echo ""
    echo -e "${GREEN}[信息] 正在编译项目...${NC}"
    mvn clean package -DskipTests

    if [ $? -ne 0 ]; then
        echo -e "${RED}[错误] 项目编译失败${NC}"
        exit 1
    fi

    echo ""
    echo -e "${GREEN}[信息] 正在启动应用...${NC}"
    echo ""

    JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)

    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}[错误] 未找到可执行的 jar 文件${NC}"
        exit 1
    fi

    echo -e "${GREEN}[信息] 启动应用: $JAR_FILE${NC}"
    echo ""
    echo "========================================"
    echo "  应用启动中..."
    echo "  - 应用地址: http://localhost:8080"
    echo "  - 接口文档: http://localhost:8080/doc.html"
    echo "  按 Ctrl+C 停止应用"
    echo "========================================"
    echo ""

    java -jar "$JAR_FILE"
}

# ========== 选择启动模式 ==========

echo "请选择启动模式:"
echo "  1. Docker 启动 (推荐，需要安装 Docker)"
echo "  2. 本地开发启动 (需要本地 MySQL、Redis、RabbitMQ)"
echo "  3. 仅启动后端 (使用已有数据库)"
echo ""
read -p "请输入选择 (1/2/3): " choice

case $choice in
    1)
        docker_start
        ;;
    2)
        local_start
        ;;
    3)
        app_start
        ;;
    *)
        echo -e "${RED}[错误] 无效的选择${NC}"
        exit 1
        ;;
esac
