# Blog Backend 项目使用指南

## 一、项目简介

这是一个基于 Spring Boot 的博客系统后端，集成了 AI 智能助理、消息队列、WebSocket 实时通知等功能，适合作为简历项目展示。

### 技术栈
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7
- RabbitMQ 3
- WebSocket (STOMP)
- JWT 认证
- 通义千问 AI API
- Docker + Docker Compose

---

## 二、环境准备

### 2.1 本地开发环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | Java 开发环境 |
| Maven | 3.6+ | 项目构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| RabbitMQ | 3.8+ | 消息队列 |

### 2.2 Docker 环境（推荐）

- Docker Desktop (Windows/Mac)
- Docker Compose

---

## 三、快速启动

### 方式一：Docker 一键启动（推荐）

```bash
# 进入项目目录
cd blog-backend

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

启动后访问：
- 应用地址：http://localhost:8080
- 接口文档：http://localhost:8080/doc.html
- RabbitMQ管理：http://localhost:15672 (guest/guest)

### 方式二：本地启动

#### 1. 初始化数据库

```bash
mysql -u root -p < src/main/resources/schema.sql
```

#### 2. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为你的密码
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

ai:
  api-key: your-api-key-here  # 通义千问 API Key
```

#### 3. 启动服务

```bash
# 编译项目
mvn clean package -DskipTests

# 启动应用
java -jar target/blog-backend-1.0.0.jar
```

### 方式三：使用启动脚本

```bash
# Windows
双击 start.bat

# Linux/Mac
chmod +x start.sh
./start.sh
```

---

## 四、功能模块说明

### 4.1 用户模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/user/register | POST | 用户注册 | 否 |
| /api/user/login | POST | 用户登录 | 否 |
| /api/user/info | GET | 获取当前用户信息 | 是 |
| /api/user/public/{userId} | GET | 获取用户公开信息 | 否 |
| /api/user/update | PUT | 更新用户信息 | 是 |

**登录返回 Token**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**使用 Token**：
在请求头中添加：`Authorization: Bearer <token>`

### 4.2 文章模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/article/create | POST | 发布文章 | 是 |
| /api/article/list | GET | 文章列表 | 否 |
| /api/article/detail/{id} | GET | 文章详情 | 否 |
| /api/article/update/{id} | PUT | 更新文章 | 是 |
| /api/article/delete/{id} | DELETE | 删除文章 | 是 |
| /api/article/like/{id} | POST | 点赞/取消点赞 | 是 |
| /api/article/user/{userId} | GET | 获取用户文章 | 否 |

### 4.3 评论模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/comment/add | POST | 添加评论 | 是 |
| /api/comment/list/{articleId} | GET | 获取评论 | 否 |
| /api/comment/delete/{id} | DELETE | 删除评论 | 是 |

**评论支持嵌套回复**：
```json
{
  "articleId": 1,
  "parentId": null,        // 顶级评论
  "replyUserId": null,     // 回复的用户ID
  "content": "评论内容"
}
```

### 4.4 收藏模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/favorite/toggle/{articleId} | POST | 收藏/取消收藏 | 是 |
| /api/favorite/check/{articleId} | GET | 检查是否已收藏 | 是 |
| /api/favorite/list | GET | 获取用户收藏列表 | 是 |

### 4.5 关注模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/follow/toggle/{userId} | POST | 关注/取消关注 | 是 |
| /api/follow/check/{userId} | GET | 检查是否已关注 | 是 |
| /api/follow/stats/{userId} | GET | 获取用户关注统计 | 否 |

### 4.6 标签模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/tag/hot | GET | 获取热门标签 | 否 |
| /api/tag/list | GET | 获取所有标签 | 否 |

### 4.7 分类模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/category/list | GET | 获取分类列表 | 否 |
| /api/category/{id} | GET | 获取分类详情 | 否 |

### 4.8 浏览历史模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/history/list | GET | 获取浏览历史 | 是 |
| /api/history/clear | DELETE | 清空浏览历史 | 是 |

### 4.9 热门文章模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/hot/list | GET | 获取热门文章 | 否 |

### 4.10 搜索模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/search/articles | POST | 搜索文章 | 否 |

**搜索请求体**：
```json
{
  "keyword": "Spring Boot",
  "categoryId": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 4.11 AI 智能助理模块

| 接口 | 方法 | 说明 | 需要登录 |
|------|------|------|----------|
| /api/ai/chat | POST | AI对话（SSE流式输出） | 否 |
| /api/ai/history/{sessionId} | GET | 获取对话历史 | 否 |
| /api/ai/history/{sessionId} | DELETE | 清除对话历史 | 否 |

**SSE 流式对话示例**：
```javascript
const eventSource = new EventSource('/api/ai/chat');

// 使用 POST 请求需要手动处理
fetch('/api/ai/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: '什么是Spring Boot?',
    sessionId: 'optional-session-id'
  })
});
```

**SSE 事件类型**：
- `start`: 对话开始，返回 sessionId
- `message`: 流式消息内容
- `complete`: 对话完成
- `error`: 错误信息

---

## 五、数据库设计

### 5.1 表结构

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| article | 文章表 |
| comment | 评论表 |
| category | 分类表 |
| tag | 标签表 |
| article_tag | 文章标签关联表 |
| follow | 关注表 |
| favorite | 收藏表 |
| browse_history | 浏览历史表 |

### 5.2 ER 图

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

## 六、Redis 使用场景

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

## 七、消息队列说明

### 7.1 交换机和队列

| 交换机 | 队列 | 路由键 | 用途 |
|--------|------|--------|------|
| notification.exchange | notification.like | notification.like | 点赞通知 |
| notification.exchange | notification.comment | notification.comment | 评论通知 |
| notification.exchange | notification.follow | notification.follow | 关注通知 |

### 7.2 通知消息格式

```json
{
  "targetUserId": 1,
  "fromUserId": 2,
  "fromUsername": "zhangsan",
  "fromNickname": "张三",
  "type": "LIKE",
  "referenceId": 100,
  "content": "赞了你的文章",
  "createdAt": "2024-01-01 12:00:00"
}
```

---

## 八、WebSocket 使用说明

### 8.1 连接地址

```
http://localhost:8080/ws
```

### 8.2 订阅通知

```javascript
// 使用 SockJS + STOMP
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  // 订阅用户专属通知
  stompClient.subscribe('/user/queue/notifications', function(message) {
    const notification = JSON.parse(message.body);
    console.log('收到通知:', notification);
  });
});
```

---

## 九、接口限流说明

### 9.1 限流规则

| 接口 | 时间窗口 | 最大请求数 | 说明 |
|------|----------|------------|------|
| /api/user/register | 60秒 | 5次 | 防止恶意注册 |
| /api/user/login | 60秒 | 10次 | 防止暴力破解 |
| /api/article/create | 60秒 | 5次 | 防止刷文章 |
| /api/ai/chat | 60秒 | 20次 | 防止AI滥用 |

### 9.2 自定义限流

```java
@RateLimit(time = 60, count = 10, message = "请求过于频繁")
public Result<?> yourMethod() {
    // ...
}
```

---

## 十、常见问题

### 10.1 启动失败：端口被占用

```bash
# 查找占用端口的进程
netstat -ano | findstr :8080

# 杀死进程
taskkill /F /PID <进程ID>
```

### 10.2 启动失败：数据库连接失败

1. 检查 MySQL 是否启动
2. 检查 `application.yml` 中的数据库配置
3. 检查数据库 `blog_db` 是否存在

### 10.3 启动失败：Redis 连接失败

1. 检查 Redis 是否启动
2. 检查 `application.yml` 中的 Redis 配置

### 10.4 启动失败：RabbitMQ 连接失败

1. 检查 RabbitMQ 是否启动
2. 检查 `application.yml` 中的 RabbitMQ 配置
3. 访问 http://localhost:15672 检查 RabbitMQ 状态

### 10.5 AI 接口调用失败

1. 检查 `application.yml` 中的 `ai.api-key` 配置
2. 确保 API Key 有效且有余额
3. 检查网络连接是否正常

---

## 十一、开发建议

### 11.1 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一异常处理
- 统一返回格式

### 11.2 性能优化

- 使用 Redis 缓存热点数据
- 使用连接池管理数据库连接
- 使用线程池处理异步任务

### 11.3 安全建议

- 生产环境修改 JWT Secret
- 生产环境修改数据库密码
- 生产环境修改 Redis 密码
- 使用 HTTPS

---

## 十二、简历写法建议

### 项目名称
**博客系统后端** - 基于 Spring Boot 的全功能博客平台

### 技术栈
Spring Boot, MyBatis-Plus, MySQL, Redis, RabbitMQ, WebSocket, JWT, Docker

### 项目亮点

1. **AI 智能助理**: 对接通义千问 API，基于 SSE 实现流式对话，支持上下文理解
2. **消息队列 + WebSocket**: RabbitMQ 异步解耦 + WebSocket 实时推送，实现点赞/评论/关注通知
3. **Redis 多场景应用**: 缓存、计数器、Sorted Set 热榜、限流、幂等控制
4. **接口设计**: AOP + 自定义注解实现限流和幂等，声明式配置，代码简洁
5. **容器化部署**: Docker + docker-compose 一键部署，环境一致

### 职责描述

- 负责后端架构设计和核心功能开发
- 实现 AI 智能助理模块，集成 SSE 流式输出
- 设计消息队列架构，实现异步通知系统
- 使用 Redis 实现缓存、限流、幂等等多种场景
- 编写单元测试，保证代码质量

---

## 十三、联系方式

如有问题，请联系项目维护者。

---

**最后更新**: 2024年1月
