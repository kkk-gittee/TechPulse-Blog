# Blog Backend

基于 Spring Boot + MyBatis-Plus + Redis + RabbitMQ + WebSocket + JWT + AI 的博客系统后端

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **实时通信**: WebSocket (STOMP)
- **认证**: JWT
- **接口文档**: Knife4j 4.3.0
- **容器化**: Docker + Docker Compose
- **HTTP客户端**: OkHttp
- **AI能力**: 通义千问 API
- **构建工具**: Maven

## 功能模块

- [x] 用户模块 (注册/登录/个人信息)
- [x] 文章模块 (CRUD/分页/搜索)
- [x] 评论模块 (嵌套评论/回复)
- [x] 点赞模块 (Redis计数器)
- [x] 收藏模块
- [x] 分类模块
- [x] 标签模块
- [x] 关注模块
- [x] 浏览历史模块
- [x] 热门文章排行榜 (Redis Sorted Set)
- [x] 文章搜索模块 (模糊搜索)
- [x] 接口限流 (Redis + AOP)
- [x] 接口幂等性 (Redis + AOP)
- [x] 全局异常处理
- [x] **AI智能助理 (SSE流式对话)**
- [x] **消息队列通知 (RabbitMQ)**
- [x] **WebSocket实时推送**

## 项目亮点

### 1. AI 智能助理（SSE 流式对话）
- **流式输出**: 基于 SSE 实现打字机效果，提升用户体验
- **上下文对话**: Redis 存储对话历史，支持多轮对话
- **每日限额**: 防止 API 滥用，支持配额管理
- **异步处理**: 线程池异步调用，避免阻塞主线程

### 2. 消息队列 + WebSocket 实时通知
- **异步解耦**: RabbitMQ 异步处理通知消息
- **实时推送**: WebSocket STOMP 协议推送
- **多类型通知**: 点赞、评论、关注通知
- **消息持久化**: RabbitMQ 消息持久化，保证不丢失

### 3. Redis 多场景应用
- **文章缓存**: 热门文章缓存，提升查询性能
- **点赞计数**: Redis计数器，高效统计
- **热门排行**: Sorted Set 实现热榜
- **接口限流**: 滑动窗口限流，防止接口刷取
- **对话存储**: 存储 AI 对话历史
- **幂等控制**: 防止重复提交

### 4. 接口幂等性
- **防重复提交**: 基于 Redis 实现幂等键
- **注解式配置**: @Idempotent 注解，使用简单
- **自动过期**: 支持配置过期时间

### 5. JWT 无状态认证
- 支持多端登录
- Token 自动过期
- 拦截器统一鉴权

### 6. 嵌套评论系统
- 支持评论回复
- 树形结构展示
- 递归查询

### 7. 关注/粉丝系统
- 用户互相关注
- 粉丝/关注数统计

### 8. 接口限流
- 基于 Redis + AOP 实现
- 注解式配置，使用简单
- 支持自定义限流规则

### 9. 全局异常处理
- 参数校验异常
- 业务异常
- 系统异常统一处理

## 快速开始

### 方式一：Docker 部署（推荐）

```bash
# 一键启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

启动后访问:
- 接口文档: http://localhost:8080/doc.html
- 应用地址: http://localhost:8080

### 方式二：本地开发

#### 1. 环境要求
- JDK 1.8+
- MySQL 8.0+
- Redis
- Maven 3.6+

#### 2. 初始化数据库
```bash
mysql -u root -p < src/main/resources/schema.sql
```

#### 3. 修改配置
编辑 `src/main/resources/application.yml`

#### 4. 启动项目
```bash
mvn spring-boot:run
```

## API 接口

### 用户模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/user/register | 用户注册 | 否 |
| POST | /api/user/login | 用户登录 | 否 |
| GET | /api/user/info | 获取当前用户信息 | 是 |
| GET | /api/user/public/{userId} | 获取用户公开信息 | 否 |
| PUT | /api/user/update | 更新用户信息 | 是 |

### 文章模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/article/create | 发布文章 | 是 |
| GET | /api/article/list | 文章列表 | 否 |
| GET | /api/article/detail/{id} | 文章详情 | 否 |
| PUT | /api/article/update/{id} | 更新文章 | 是 |
| DELETE | /api/article/delete/{id} | 删除文章 | 是 |
| POST | /api/article/like/{id} | 点赞/取消点赞 | 是 |
| GET | /api/article/user/{userId} | 获取用户文章 | 否 |

### 评论模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/comment/add | 添加评论 | 是 |
| GET | /api/comment/list/{articleId} | 获取评论 | 否 |
| DELETE | /api/comment/delete/{id} | 删除评论 | 是 |

### 收藏模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/favorite/toggle/{articleId} | 收藏/取消收藏 | 是 |
| GET | /api/favorite/check/{articleId} | 检查是否已收藏 | 是 |
| GET | /api/favorite/list | 获取用户收藏列表 | 是 |

### 关注模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/follow/toggle/{userId} | 关注/取消关注 | 是 |
| GET | /api/follow/check/{userId} | 检查是否已关注 | 是 |
| GET | /api/follow/stats/{userId} | 获取用户关注统计 | 否 |

### 标签模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /api/tag/hot | 获取热门标签 | 否 |
| GET | /api/tag/list | 获取所有标签 | 否 |

### 分类模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /api/category/list | 获取分类列表 | 否 |
| GET | /api/category/{id} | 获取分类详情 | 否 |

### 浏览历史模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /api/history/list | 获取浏览历史 | 是 |
| DELETE | /api/history/clear | 清空浏览历史 | 是 |

### 热门文章模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /api/hot/list | 获取热门文章 | 否 |

### 搜索模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/search/articles | 搜索文章 | 否 |

### AI智能助理模块
| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /api/ai/chat | AI对话（SSE流式输出） | 否 |
| GET | /api/ai/history/{sessionId} | 获取对话历史 | 否 |
| DELETE | /api/ai/history/{sessionId} | 清除对话历史 | 否 |

## 项目结构

```
blog-backend/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/blog/
    │   │   ├── BlogApplication.java
    │   │   ├── annotation/           # 自定义注解
    │   │   │   └── RateLimit.java
    │   │   ├── aspect/               # AOP切面
    │   │   │   └── RateLimitAspect.java
    │   │   ├── common/               # 公共类
    │   │   │   ├── Result.java
    │   │   │   ├── Constants.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── config/               # 配置类
    │   │   ├── entity/               # 实体类
    │   │   ├── mapper/               # Mapper接口
    │   │   ├── dto/                  # 数据传输对象
    │   │   ├── service/              # 业务逻辑层
    │   │   ├── controller/           # 控制器层
    │   │   └── utils/                # 工具类
    │   └── resources/
    │       ├── application.yml
    │       └── schema.sql
    └── test/
        └── java/com/blog/
            ├── BlogApplicationTests.java
            └── service/
                └── UserServiceTest.java
```

## 简历亮点

1. **AI 智能助理**: 对接通义千问 API，SSE 流式输出，支持上下文对话
2. **消息队列 + WebSocket**: RabbitMQ 异步解耦 + WebSocket 实时推送
3. **Redis 多场景应用**: 缓存、计数器、Sorted Set 热榜、限流、幂等控制
4. **JWT 无状态认证**: 支持多端登录，Token 自动过期
5. **AOP + 注解实现限流/幂等**: 声明式设计，代码简洁
6. **嵌套评论系统**: 树形结构，递归查询
7. **Docker 容器化部署**: 一键部署，环境一致
8. **全局异常处理**: 统一异常处理，友好错误提示

## 后续计划

- [ ] Elasticsearch 全文搜索
- [ ] 图片上传 (OSS)
- [ ] GitHub Actions CI/CD
- [ ] 文章导出 PDF
