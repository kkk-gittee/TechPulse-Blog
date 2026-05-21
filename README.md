# TechPulse 技术脉动

基于 Spring Boot + Vue 3 的全栈博客系统，集成 AI 智能问答（RAG + SSE 流式）、消息队列异步通知、Redis 缓存架构。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Java 17 / Spring Boot 2.7 / MyBatis-Plus / MySQL 8.0 / Redis / RabbitMQ |
| 前端 | Vue 3 / Vite / Tailwind CSS 4 / Pinia |
| AI | 通义千问 API / LangChain4j / SSE 流式输出 |
| 部署 | Docker / Docker Compose |

## 核心功能

- 文章 CRUD + Markdown 编辑器
- AI 智能问答（RAG 增强 + SSE 逐 token 流式输出）
- 点赞 / 收藏 / 关注社交体系
- RabbitMQ 解耦 + WebSocket 实时通知
- Redis 三级缓存防护（穿透 / 击穿 / 雪崩）
- AOP 通用限流 `@RateLimit` + 幂等 `@Idempotent`
- 全文搜索、暗色模式、图片上传、浏览历史

## 项目亮点

### 1. Redis 缓存架构
空值缓存防穿透、SETNX 分布式锁防击穿、TTL 随机抖动防雪崩。Redis 计数器 + 定时批量同步 MySQL。

### 2. 消息队列解耦通知
RabbitMQ 异步处理点赞 / 评论 / 关注事件，WebSocket STOMP 实时推送。无 MQ 时自动降级为日志记录。

### 3. AI 智能问答（RAG + SSE）
基于 LangChain4j 实现文章向量检索 + 上下文注入，SSE 流式输出逐 token 推送，Redis 原子计数器每日限流。

### 4. AOP 通用组件
`@RateLimit` 滑动窗口限流（Redis Sorted Set）、`@Idempotent` 幂等防重复（Redis SETNX），一行注解复用。

## 快速开始

### Docker 部署（推荐）

```bash
# 设置 AI API Key（可选，不设置则 AI 功能不可用）
export AI_API_KEY=your-api-key

# 一键启动
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

启动后访问：
- 前端：http://localhost:5173
- 后端 API 文档：http://localhost:8080/doc.html

### 本地开发

**环境要求**：JDK 17+ / MySQL 8.0+ / Redis / Node.js 18+ / Maven 3.6+

```bash
# 1. 初始化数据库
mysql -u root -p < blog-backend/src/main/resources/schema.sql

# 2. 启动后端
cd blog-backend
mvn spring-boot:run -DskipTests

# 3. 启动前端
cd blog-frontend
npm install
npm run dev
```

## 项目结构

```
TechPulse-Blog/
├── blog-backend/          # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── annotation/    # 自定义注解
│   │   ├── aspect/        # AOP 切面（限流/幂等）
│   │   ├── common/        # 公共类（Result/异常处理）
│   │   ├── config/        # 配置（Redis/CORS/WebSocket）
│   │   ├── consumer/      # RabbitMQ 消费者
│   │   ├── controller/    # REST 接口
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # MyBatis-Plus Mapper
│   │   ├── service/       # 业务逻辑层
│   │   └── utils/         # 工具类（Redis/JWT）
│   └── src/main/resources/
│       ├── application.yml
│       └── schema.sql
├── blog-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── api/           # Axios 请求模块
│   │   ├── components/    # 组件
│   │   ├── composables/   # 组合式函数
│   │   ├── stores/        # Pinia 状态管理
│   │   └── views/         # 页面视图
│   └── vite.config.js
├── docker-compose.yml
└── docs/
```

## API 接口

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户 | POST | /api/user/register | 注册 |
| 用户 | POST | /api/user/login | 登录 |
| 文章 | POST | /api/article/create | 发布文章 |
| 文章 | GET | /api/article/list | 文章列表 |
| 文章 | GET | /api/article/detail/{id} | 文章详情 |
| 评论 | POST | /api/comment/add | 添加评论 |
| 评论 | GET | /api/comment/list/{articleId} | 获取评论 |
| 点赞 | POST | /api/article/like/{id} | 点赞/取消 |
| 收藏 | POST | /api/favorite/toggle/{articleId} | 收藏/取消 |
| 关注 | POST | /api/follow/toggle/{userId} | 关注/取消 |
| 搜索 | POST | /api/search/articles | 搜索文章 |
| AI | POST | /api/ai/chat | AI 对话 (SSE) |
| AI | GET | /api/ai/history/{sessionId} | 对话历史 |

完整 API 文档见 Knife4j Swagger UI：http://localhost:8080/doc.html

## License

MIT
