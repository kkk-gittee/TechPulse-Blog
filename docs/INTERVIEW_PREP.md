# TechPulse 面试准备手册

---

## 一、项目概述（30 秒版本）

**TechPulse 技术脉动** — 基于 Spring Boot + Vue 3 的全栈博客系统，集成 AI 智能问答（RAG + SSE 流式）、消息队列异步通知、Redis 缓存架构，支持 Docker 一键部署。

**技术栈**：Java 17 / Spring Boot 2.7 / MyBatis-Plus / MySQL / Redis / RabbitMQ / Vue 3 / Vite / Tailwind CSS / Docker / LangChain4j

**核心功能**：文章 CRUD + Markdown 编辑、AI 智能问答、点赞/收藏/关注社交体系、实时通知、全文搜索、暗色模式、图片上传、浏览历史

---

## 二、四大亮点详解

### 亮点 1：Redis 缓存架构

#### 一句话
> 设计三级缓存防护体系：空值缓存防穿透、分布式互斥锁防击穿、TTL 随机抖动防雪崩。

#### 面试展开（问题 → 方案 → 效果）

**缓存穿透（Cache Penetration）**
- 问题：用不存在的 ID 请求，缓存永远查不到，全部打到 MySQL
- 方案：空值缓存。DB 查不到时存 `__NULL__` 标记，TTL 60 秒
- 效果：恶意请求不再击穿到数据库
- 代码位置：`CacheService.getOrLoad()`

**缓存击穿（Cache Breakdown）**
- 问题：热点 key 过期瞬间，100 个请求同时打到 DB
- 方案：Redis `SETNX` 分布式互斥锁。第一个请求加锁后查 DB 回填缓存，其他请求等待 50ms 后重试
- 效果：同一时刻只有一个线程查 DB
- 代码位置：`CacheService.getOrLoad()` 中的 `tryLock` 逻辑

**缓存雪崩（Cache Avalanche）**
- 问题：大量 key 同时过期，瞬间压垮 DB
- 方案：TTL 加随机抖动（±15%），过期时间分散
- 效果：过期请求均匀分布，不会瞬间集中
- 代码位置：`CacheService.randomJitter()`

#### 常见追问

Q：空值缓存会不会占满 Redis？
> 不会。空值 TTL 只有 60 秒，远短于正常缓存。而且正常业务中查不存在的 ID 是少数。

Q：为什么用 SETNX 不用 Redisson？
> 简历项目用原生 SETNX 展示原理。生产环境会用 Redisson 的看门狗机制（自动续期），避免业务没处理完锁就过期。

Q：Redis 挂了怎么办？
> `RedisUtils` 有内存降级机制，自动切换到 `ConcurrentHashMap`。内存缓存有 10000 条上限防止 OOM。分布式功能（限流、幂等）会降级，但核心读写不受影响。

---

### 亮点 2：消息队列解耦通知

#### 一句话
> 使用 RabbitMQ 解耦点赞、评论、关注等社交事件，异步推送 WebSocket 实时通知。

#### 架构图

```
用户点赞文章
    ↓
LikeService.toggleLike()
    ↓
NotificationService.sendLikeNotification()
    ↓
RabbitTemplate.convertAndSend()
    ↓
RabbitMQ (持久化队列)
    ↓
NotificationConsumer (消费者)
    ↓
SimpMessagingTemplate.convertAndSendToUser()
    ↓
WebSocket 推送到前端
```

#### 面试展开

**为什么不用同步调用？**
1. **耦合**：通知失败会影响点赞接口响应
2. **性能**：WebSocket 推送可能慢，拖慢主流程
3. **扩展性**：加邮件/站内信通知要改业务代码

**用了 MQ 之后**：业务只管发消息，立即返回。通知失败不影响主流程。Consumer 挂了消息不丢（RabbitMQ 持久化）。

**降级策略**：`rabbitmq.enabled: false` 默认关闭。没启动 RabbitMQ 时，通知只记录日志，不影响其他功能。用 `@ConditionalOnBean(RabbitTemplate.class)` 实现。

#### 常见追问

Q：消息丢失怎么办？
> RabbitMQ 支持消息持久化（durable exchange + queue）。Consumer 用自动确认，处理完才 ack。Consumer 挂了消息重新入队。

Q：为什么不用 Kafka？
> Kafka 适合大数据量的日志/事件流，博客系统的通知量用 RabbitMQ 足够，部署也更简单。面试时可以说"了解 Kafka 的优势，但根据场景选择了更轻量的 RabbitMQ"。

---

### 亮点 3：AI 智能问答（RAG + SSE）

#### 一句话
> 集成通义千问大模型，基于 LangChain4j 实现 RAG 增强问答（文章向量检索 + 上下文注入），SSE 流式输出。

#### 什么是 RAG？

RAG = Retrieval Augmented Generation（检索增强生成）

直接问大模型"我的博客怎么用"，它不知道。RAG 的做法：
1. **检索**：把问题转成向量，检索最相关的 5 篇文章片段
2. **增强**：把片段作为上下文塞进 Prompt
3. **生成**：大模型基于上下文回答

```
用户输入 → 向量化 → 检索相关文章 → 拼接 Prompt → 调用通义千问 → 流式输出
```

#### SSE 流式输出

传统模式：用户等 5 秒 → 一次性拿到完整回答
SSE 模式：用户边输入边看到文字逐个出现，体验更好

```java
// 服务端：每收到一个 token 就推送
tokenStream
    .onNext(token -> emitter.send(token, MediaType.TEXT_PLAIN))
    .onComplete(() -> emitter.complete())
    .start();
```

```javascript
// 前端：逐 token 追加到页面
const reader = response.body.getReader()
while (true) {
    const { done, value } = await reader.read()
    if (done) break
    appendToPage(new TextDecoder().decode(value))
}
```

#### 每日限流

Redis 原子计数器，一次 Lua 脚本完成 INCR + EXPIRE，避免竞态条件：

```java
long currentCount = redisUtils.incrementWithExpire(DAILY_LIMIT_KEY + userId, 86400);
if (currentCount > aiConfig.getDailyLimit()) {
    redisUtils.increment(DAILY_LIMIT_KEY + userId, -1);  // 回滚
    throw new RuntimeException("今日AI调用次数已达上限");
}
```

#### 常见追问

Q：为什么用 SSE 不用 WebSocket？
> SSE 是单向的（服务端→客户端），WebSocket 是双向的。AI 生成是"用户提问→服务端回答"，单向就够了，SSE 更简单，基于 HTTP 不需要额外握手。

Q：向量检索用的什么？
> LangChain4j 的 `ContentRetriever`，底层用通义千问的 `text-embedding-v2` 模型将文章向量化，存入内存向量存储。生产环境会用 Milvus 或 Elasticsearch。

---

### 亮点 4：AOP 通用组件

#### 一句话
> 基于自定义注解 + AOP 实现 `@RateLimit` 和 `@Idempotent`，一处声明即可复用。

#### @RateLimit 滑动窗口限流

```java
// 使用：一行注解
@RateLimit(time = 60, count = 5, message = "发布过于频繁")
public Result<Void> createArticle(...) { ... }
```

实现原理：Redis Sorted Set，score 是时间戳，member 是请求 ID。

```java
@Around("@annotation(rateLimit)")
public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
    String key = "rate:" + getMethodKey(joinPoint);
    long windowStart = System.currentTimeMillis() - rateLimit.time() * 1000;
    
    redisUtils.zRemoveRangeByScore(key, 0, windowStart);  // 清理窗口外的
    long count = redisUtils.zCard(key);  // 统计窗口内请求数
    
    if (count >= rateLimit.count()) {
        throw new BusinessException(rateLimit.message());
    }
    
    redisUtils.zAdd(key, UUID.randomUUID().toString(), System.currentTimeMillis());
    return joinPoint.proceed();
}
```

#### @Idempotent 幂等防重复

```java
// 使用
@Idempotent(expire = 10, message = "请勿重复提交")
public Result<Void> addComment(...) { ... }
```

用 Redis `SETNX` 加锁，相同请求在过期时间内只能执行一次。

#### 为什么用 AOP？

没有 AOP 时，每个接口都要手写限流逻辑（重复代码）。AOP 之后，一行注解搞定，业务代码完全不感知。

#### 常见追问

Q：限流算法为什么选滑动窗口不用令牌桶？
> 滑动窗口用 Redis Sorted Set 实现简单直观。令牌桶适合需要"突发流量"的场景，博客系统用滑动窗口足够。了解两者的区别，根据场景选择。

---

## 三、计数器同步方案（单独讲）

### 为什么不能直接写 MySQL？

热点文章被点赞 1000 次 = 1000 次 `UPDATE`，MySQL 扛不住。

### 方案：Redis 计数器 + 定时批量同步

```
点赞 → Redis INCR → 标记脏数据 → 返回 (不碰 MySQL)
                    ↓ (每5分钟)
               定时任务扫描脏数据
                    ↓
               批量 UPDATE MySQL
                    ↓
               清除 Redis 增量
```

### 显示时的计数准确性

```
显示点赞数 = DB 值 + Redis 增量
```

```java
int likeDelta = counterSyncService.getLikeCount(articleId);
int currentLikes = article.getLikeCount() != null ? article.getLikeCount() : 0;
article.setLikeCount(currentLikes + likeDelta);
```

### 常见追问

Q：Redis 和 DB 的计数不一致怎么办？
> 设计目标是"最终一致性"，不是强一致。5 分钟内的误差（最多几十个赞）对博客场景可接受。如果需要强一致，可以在读取时也查 DB，但会增加复杂度。

Q：定时任务间隔 5 分钟会不会太长？
> 可以根据业务调整。5 分钟是平衡点：太短则批量优势没了，太长则误差大。面试时说"根据业务容忍度调整"即可。

---

## 四、项目不足与改进方向

面试官问"有什么不足"时，坦诚回答比假装完美更有说服力：

| 不足 | 改进方向 | 面试话术 |
|------|----------|----------|
| Spring Boot 2.7 偏旧 | 升级到 3.x (Jakarta EE) | "了解 3.x 的变化，计划升级" |
| 测试覆盖不完整 | 加 Controller 集成测试 (MockMvc) | "目前有 94 个 Service 层单测，缺少集成测试" |
| 没有监控告警 | 接入 Spring Actuator + Prometheus | "了解可观测性的重要性" |
| 单体架构 | 未来可拆微服务 | "当前规模单体够用，流量大了再拆" |
| 没有 CI/CD | 加 GitHub Actions | "了解 CI/CD 流程" |
| 向量检索用内存 | 用 Milvus/ES 替代 | "生产环境会用专业向量数据库" |

---

## 五、面试话术模板

### 自我介绍（1 分钟）

> 我是 XXX，大三学生。我有一个全栈博客项目 TechPulse，用 Spring Boot + Vue 3 开发。项目有几个亮点：一是 Redis 缓存架构，设计了三级防护体系（穿透/击穿/雪崩）；二是用 RabbitMQ 解耦通知系统，异步推送 WebSocket 实时通知；三是集成了通义千问大模型做 RAG 增强问答，用 SSE 流式输出提升体验；四是用 AOP 实现了通用的限流和幂等注解。

### 回答技术问题的四段式

1. **问题**：这个场景有什么挑战？
2. **方案**：我怎么解决的？
3. **效果**：解决了什么问题？
4. **替代方案**：还有什么其他选择？为什么没用？

### 被问到不会的问题

> 这个点我了解但没有深入实践过。我的理解是 XXX，不知道是否准确？

不要硬编，诚实说"了解但没深入"比胡说强。

---

## 六、关键代码位置速查

| 功能 | 文件位置 |
|------|----------|
| 缓存三级防护 | `blog-backend/.../service/CacheService.java` |
| 计数器同步 | `blog-backend/.../service/CounterSyncService.java` |
| 点赞服务 | `blog-backend/.../service/impl/LikeServiceImpl.java` |
| 通知服务 | `blog-backend/.../service/impl/NotificationServiceImpl.java` |
| RabbitMQ 消费者 | `blog-backend/.../consumer/NotificationConsumer.java` |
| AI 服务 | `blog-backend/.../service/impl/AiServiceImpl.java` |
| AI 控制器 (SSE) | `blog-backend/.../controller/AiController.java` |
| 限流切面 | `blog-backend/.../aspect/RateLimitAspect.java` |
| 幂等切面 | `blog-backend/.../aspect/IdempotentAspect.java` |
| Redis 工具类 | `blog-backend/.../utils/RedisUtils.java` |
| JWT 工具类 | `blog-backend/.../utils/JwtUtils.java` |
| 认证拦截器 | `blog-backend/.../controller/AuthInterceptor.java` |
| 前端路由守卫 | `blog-frontend/.../router/index.js` |
| 前端用户状态 | `blog-frontend/.../stores/user.js` |
| WebSocket 连接 | `blog-frontend/.../composables/useWebSocket.js` |
| SSE 流式写入 | `blog-frontend/.../composables/useSSEWriter.js` |

---

## 七、一页纸速记卡

```
┌─────────────────────────────────────────────┐
│  TechPulse 技术脉动                          │
│  Spring Boot + Vue 3 全栈博客               │
├─────────────────────────────────────────────┤
│  亮点1: Redis 三级缓存防护                   │
│  空值缓存→防穿透 | SETNX锁→防击穿            │
│  TTL抖动→防雪崩 | 计数器→Redis+定时同步MySQL  │
├─────────────────────────────────────────────┤
│  亮点2: RabbitMQ 解耦通知                    │
│  点赞/评论/关注→MQ→Consumer→WebSocket推送    │
│  异步解耦 | 消息持久化 | 降级(无MQ时记日志)   │
├─────────────────────────────────────────────┤
│  亮点3: AI RAG + SSE                        │
│  通义千问 + LangChain4j | 向量检索文章上下文  │
│  SSE逐token流式输出 | Redis原子计数器限流     │
├─────────────────────────────────────────────┤
│  亮点4: AOP 通用组件                         │
│  @RateLimit 滑动窗口(Redis ZSet)             │
│  @Idempotent 幂等(Redis SETNX)              │
│  一行注解复用，零业务侵入                     │
└─────────────────────────────────────────────┘
```
