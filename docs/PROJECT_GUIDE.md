# Blog System - Project Documentation

## 一、项目简介

基于 Spring Boot + Vue 3 的全栈博客系统，集成 AI 智能助理、消息队列、WebSocket 实时通知等功能。

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7 |
| 消息队列 | RabbitMQ | 3 |
| 实时通信 | WebSocket (STOMP) | - |
| 认证 | JWT | 0.11.5 |
| 接口文档 | Knife4j | 4.3.0 |
| 前端框架 | Vue 3 + Composition API | 3.4 |
| 构建工具 | Vite | 5.2 |
| UI 组件库 | Element Plus | 2.7 |
| 状态管理 | Pinia | 2.1 |
| 容器化 | Docker + Docker Compose | - |

---

## 二、功能模块

### 核心功能
1. **用户模块** - 注册/登录/个人信息管理，JWT 无状态认证
2. **文章模块** - CRUD/分页/Markdown 编辑器/封面图
3. **评论模块** - 嵌套评论/回复/树形结构展示
4. **点赞模块** - Redis 计数器，高效统计
5. **收藏模块** - 用户收藏管理
6. **分类/标签** - 文章分类与标签管理
7. **关注模块** - 用户互相关注/粉丝统计
8. **浏览历史** - 记录用户浏览记录

### 亮点功能
9. **AI 智能助理** - 对接通义千问 API，SSE 流式输出，支持上下文对话
10. **消息队列通知** - RabbitMQ 异步解耦，WebSocket 实时推送
11. **接口限流** - Redis + AOP 实现滑动窗口限流
12. **接口幂等性** - Redis 幂等键，防止重复提交
13. **热门排行** - Redis Sorted Set 实现热榜
14. **全文搜索** - 文章模糊搜索

---

## 三、项目亮点（简历写法）

### 项目名称
**博客系统全栈开发** - 基于 Spring Boot + Vue 3 的全功能博客平台

### 项目描述
独立设计并开发的全栈博客系统，包含用户认证、文章管理、AI 智能助理、实时通知等完整功能模块。采用前后端分离架构，后端基于 Spring Boot 提供 RESTful API，前端使用 Vue 3 构建响应式 SPA。

### 技术栈
Spring Boot, MyBatis-Plus, MySQL, Redis, RabbitMQ, WebSocket, JWT, Vue 3, Element Plus, Pinia, Vite, Docker

### 项目亮点

1. **AI 智能助理（SSE 流式对话）**
   - 对接通义千问 API，基于 SSE（Server-Sent Events）实现打字机效果流式输出
   - 使用 Redis 存储对话历史，支持多轮上下文对话
   - 实现每日调用限额，防止 API 滥用
   - 采用线程池异步处理，避免阻塞主线程

2. **消息队列 + WebSocket 实时通知**
   - 设计 RabbitMQ 消息队列架构，实现点赞、评论、关注事件的异步解耦
   - 集成 WebSocket STOMP 协议，实现客户端实时推送通知
   - 消息持久化保证不丢失，支持多类型通知（点赞/评论/关注）

3. **Redis 多场景应用**
   - 文章详情缓存（30分钟过期），提升查询性能
   - 点赞计数器，O(1) 复杂度高效统计
   - Sorted Set 实现热门文章排行榜
   - 滑动窗口限流，防止接口恶意刷取
   - 幂等键控制，防止重复提交

4. **接口设计与安全**
   - AOP + 自定义注解（@RateLimit、@Idempotent）实现声明式限流和幂等
   - JWT 无状态认证，支持多端登录，Token 自动过期
   - 全局异常处理，统一错误码和错误信息

5. **Docker 容器化部署**
   - 编写 Dockerfile 和 docker-compose.yml，一键部署全栈环境
   - MySQL、Redis、RabbitMQ、应用服务编排，健康检查机制

### 职责描述
- 负责后端架构设计和核心功能开发，设计 RESTful API 规范
- 实现 AI 智能助理模块，集成 SSE 流式输出和上下文对话
- 设计消息队列架构，实现异步通知系统
- 使用 Redis 实现缓存、限流、幂等等多种场景
- 开发 Vue 3 前端 SPA，包含 7 个核心页面和 15+ 个组件

---

## 四、快速开始

### 环境要求
- JDK 1.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Maven 3.6+

### 一键启动
```bash
# Windows
双击 start.bat

# 或手动启动
# 终端1: 启动后端
cd blog-backend
mvn spring-boot:run

# 终端2: 启动前端
cd blog-frontend
npm run dev
```

### 一键关闭
```bash
# Windows
双击 stop.bat
```

### Docker 部署（推荐）
```bash
cd blog-backend
docker-compose up -d
```

### 访问地址
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- 接口文档：http://localhost:8080/doc.html
- RabbitMQ 管理：http://localhost:15672（guest/guest）

---

## 五、项目结构

```
TestClaude/
├── blog-backend/                 # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── controller/           # 12 个控制器，33 个 API 端点
│   │   ├── service/              # 业务逻辑层
│   │   ├── mapper/               # MyBatis-Plus 数据访问层
│   │   ├── entity/               # 实体类（9 张表）
│   │   ├── dto/                  # 数据传输对象
│   │   ├── config/               # 配置类（安全、CORS、MQ、WebSocket）
│   │   ├── annotation/           # 自定义注解（@RateLimit、@Idempotent）
│   │   ├── aspect/               # AOP 切面
│   │   └── common/               # 公共类（Result、异常处理）
│   ├── src/main/resources/
│   │   ├── application.yml       # 应用配置
│   │   └── schema.sql            # 数据库初始化脚本
│   ├── docker-compose.yml        # Docker 编排
│   └── Dockerfile
│
├── blog-frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/                  # 13 个 API 模块
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── router/               # 路由配置 + 导航守卫
│   │   ├── views/                # 7 个页面组件
│   │   ├── components/           # 15+ 个可复用组件
│   │   ├── layouts/              # 布局组件
│   │   ├── composables/          # 组合式函数
│   │   └── utils/                # 工具函数
│   └── vite.config.js            # Vite 配置（代理、自动导入）
│
├── start.bat                     # 一键启动脚本
├── stop.bat                      # 一键关闭脚本
└── docs/
    └── PROJECT_GUIDE.md          # 本文档
```

---

## 六、API 接口概览

| 模块 | 端点数 | 说明 |
|------|--------|------|
| 用户 | 5 | 注册/登录/信息/更新 |
| 文章 | 7 | CRUD/点赞/用户文章 |
| 评论 | 3 | 添加/列表/删除 |
| 分类 | 2 | 列表/详情 |
| 标签 | 2 | 热门/全部 |
| 收藏 | 3 | 切换/检查/列表 |
| 关注 | 3 | 切换/检查/统计 |
| 浏览历史 | 2 | 列表/清空 |
| 热门文章 | 2 | 列表/清空 |
| 搜索 | 1 | 文章搜索 |
| AI 助手 | 3 | 对话(SSE)/历史/清空 |
| **总计** | **33** | - |

---

## 七、数据库设计

### 表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | id, username, password, nickname, avatar, email, bio |
| article | 文章表 | id, user_id, title, content, summary, cover_image, category_id, view_count, like_count |
| comment | 评论表 | id, article_id, user_id, parent_id, reply_user_id, content |
| category | 分类表 | id, name, description |
| tag | 标签表 | id, name |
| article_tag | 文章标签关联 | article_id, tag_id |
| follow | 关注表 | id, user_id, follow_user_id |
| favorite | 收藏表 | id, user_id, article_id |
| browse_history | 浏览历史 | id, user_id, article_id |

### ER 关系
```
User (1) --- (N) Article
User (1) --- (N) Comment
User (1) --- (N) Follow
User (1) --- (N) Favorite
User (1) --- (N) BrowseHistory
Article (1) --- (N) Comment
Article (N) --- (N) Tag (通过 article_tag)
Category (1) --- (N) Article
```

---

## 八、Redis 使用场景

| Key 格式 | 用途 | 过期时间 |
|----------|------|----------|
| article:detail:{id} | 文章详情缓存 | 30分钟 |
| article:view:{id} | 文章浏览量 | 永久 |
| like:{articleId} | 文章点赞数 | 永久 |
| like:{articleId}:user:{userId} | 用户点赞状态 | 永久 |
| hot:articles | 热门文章排行 | 永久 |
| ai:chat:history:{sessionId} | AI对话历史 | 24小时 |
| ai:daily:limit:{userId} | AI每日调用次数 | 24小时 |
| rate_limit:{method}:{ip} | 接口限流 | 60秒 |
| idempotent:{method}:{token} | 幂等键 | 5秒 |

---

## 九、消息队列设计

### 交换机和队列

| 交换机 | 队列 | 路由键 | 用途 |
|--------|------|--------|------|
| notification.exchange | notification.like | notification.like | 点赞通知 |
| notification.exchange | notification.comment | notification.comment | 评论通知 |
| notification.exchange | notification.follow | notification.follow | 关注通知 |

### 消息格式
```json
{
  "targetUserId": 1,
  "fromUserId": 2,
  "fromUsername": "zhangsan",
  "type": "LIKE",
  "referenceId": 100,
  "content": "赞了你的文章"
}
```

---

## 十、面试常见问题

### Q: 为什么选择 Redis 做点赞计数而不是数据库？
A: Redis 是内存数据库，读写性能是数据库的 10 万倍级别。点赞是高频操作，使用 Redis 计数器可以避免频繁的数据库写入，减轻数据库压力。同时 Redis 支持原子操作，保证并发安全。

### Q: SSE 和 WebSocket 的区别？为什么 AI 对话用 SSE？
A: SSE 是单向的（服务端到客户端），WebSocket 是双向的。AI 对话场景中，只需要服务端流式推送回答，不需要客户端频繁发送数据，SSE 更简单高效。而且 SSE 基于 HTTP，兼容性更好，不需要额外的协议升级。

### Q: 接口幂等性是怎么实现的？
A: 使用 Redis 存储幂等键，键格式为 `idempotent:{method}:{token}`，过期时间 5 秒。通过自定义注解 @Idempotent 和 AOP 切面实现。请求到达时先检查 Redis 中是否存在该键，如果存在则拒绝请求，不存在则存储键并处理请求。

### Q: 消息队列在这里的作用是什么？
A: 主要是异步解耦。点赞、评论、关注等操作会产生通知，如果同步处理会增加响应时间。使用 RabbitMQ 将通知消息异步投递，由消费者异步处理并推送给用户，提高了系统的响应速度和可靠性。

### Q: Docker Compose 编排了哪些服务？
A: 编排了 4 个服务：MySQL（数据库）、Redis（缓存）、RabbitMQ（消息队列）、应用服务。通过 depends_on 和 healthcheck 确保依赖服务先启动，使用 bridge 网络实现服务间通信，数据持久化使用 Docker Volume。
