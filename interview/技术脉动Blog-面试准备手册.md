# 技术脉动 AI Blog — 面试准备手册

> 基于简历 5 条 bullet 逐条拆解，覆盖基础概念 → 项目细节 → 原理深入 → 开放性问题

---

## 第 1 条：LangChain4j RAG + Function Calling

### 基础概念

**Q1：什么是 RAG？为什么不用纯大模型回答？**

A：RAG（Retrieval-Augmented Generation，检索增强生成）是"先搜再答"的模式。纯大模型有两个问题：
1. 知识有截止日期，不知道你博客里写了什么
2. 可能"幻觉"——一本正经地胡说

RAG 的做法是：用户提问 → 从你的知识库检索相关文档 → 把检索结果注入 prompt → 让 LLM 基于这些内容生成回答。这样回答有据可依，还能引用你的原文。

**Q2：向量检索和关键词检索有什么区别？**

A：
- 关键词检索（如 MySQL LIKE）：匹配字面文字。搜"Vue 响应式"只能找到包含这几个字的文章
- 向量检索：匹配语义。搜"Vue 响应式"能找到讲"响应式系统原理"的文章，即使没出现"响应式"三个字
- 实际项目中两者互补，RAG 用向量检索做语义匹配，效果更好

**Q3：Embedding 是什么？text-embedding-v2 输出什么？**

A：Embedding（向量化）是把文字变成一组数字（向量）的过程。text-embedding-v2 是通义千问的向量模型，输出 1536 维的浮点数数组。语义相近的文字，向量也相近（余弦相似度高）。

```
"Vue 3 响应式"  → [0.12, -0.34, 0.56, ...]  （1536 个数字）
"红烧肉做法"    → [0.89, 0.23, -0.67, ...]  （差很远）
```

**Q4：Function Calling 是什么？和 RAG 有什么区别？**

A：
- RAG：从知识库检索信息，注入 prompt 让 LLM 参考
- Function Calling：LLM 主动调用你的 Java 方法，拿到实时数据

区别：RAG 是"给你资料你自己看"，Function Calling 是"你自己去查"。

实际项目中两者配合：简单问题用 RAG 检索文章回答；需要实时数据（热门排行、分类列表）时用 Function Calling 调方法。

### 项目细节

**Q5：你的项目中文章是怎么分块的？为什么这样分？**

A：每篇文章按 500 字分块，块之间有 100 字重叠。
- 500 字：平衡了语义完整性和检索精度。太长会稀释重点，太短会丢失上下文
- 100 字重叠：防止关键信息被切断在两个块的边界

**Q6：相似度阈值 0.6 和 top 5 是怎么定的？**

A：
- top 5：取最相关的 5 个片段。太少可能遗漏信息，太多会超出 prompt 上下文窗口
- min-score 0.6：过滤掉不相关的结果。低于 0.6 的片段跟问题关系不大，塞进去反而干扰回答
- 这些参数是经验值，可以通过测试调整

**Q7：增量更新是怎么做的？为什么用 ApplicationEvent？**

A：文章发布/删除时，ArticleService 发布一个 Spring Event（ArticleSavedEvent / ArticleDeletedEvent），DocumentIndexingService 监听到事件后自动更新向量索引。

用 ApplicationEvent 的好处是松耦合——ArticleService 不需要知道向量索引的存在，只管发事件。后续加别的监听器（比如搜索索引更新）也不用改 ArticleService。

**Q8：ChatMemory 是什么？为什么不用 Redis 存对话历史？**

A：ChatMemory 是 LangChain4j 提供的对话记忆管理器。我用的是 MessageWindowChatMemory，保留最近 20 轮对话。

之前用 Redis 手动存（ZSet），后来换成 LangChain4j 原生方案，因为：
1. 它自动处理 prompt 拼接（把历史消息格式化成 LLM 能理解的格式）
2. 自动管理 token 窗口（超过 20 轮自动丢弃最早的）
3. 代码更简洁

### 原理深入

**Q9：向量相似度是怎么算的？**

A：常用余弦相似度。公式：cos(A,B) = A·B / (|A|×|B|)。值越接近 1 越相似，接近 0 越不相关。实际实现中 InMemoryEmbeddingStore 会遍历所有向量计算相似度，返回 top K。

**Q10：如果文章量很大，InMemoryEmbeddingStore 够用吗？**

A：InMemoryEmbeddingStore 适合中小规模（几万篇以内）。文章量大的话应该换成专业向量数据库（如 Milvus、Pinecone、PgVector），它们用 ANN（近似最近邻）算法加速检索，不需要遍历所有向量。

**Q11：LangChain4j 和 Spring AI 有什么区别？**

A：都是 Java 生态的 LLM 应用框架。LangChain4j 社区更成熟，Function Calling 和 RAG 支持更完善；Spring AI 是 Spring 官方出品，跟 Spring 生态集成更好。我选 LangChain4j 是因为它对通义千问的支持更好。

---

## 第 2 条：Redis 容错 + 门面模式

### 基础概念

**Q12：什么是门面模式（Facade）？你在项目中怎么用的？**

A：门面模式是把复杂的子系统封装成一个统一的简单接口。我的 RedisUtils 封装了 9 种 Redis 操作（set/get/delete/hasKey/increment 等），调用方不需要关心底层是 Redis 还是本地缓存。

```java
// 调用方只管用
redisUtils.set("key", value, 30, MINUTES);
redisUtils.increment("counter");

// 底层自动处理：尝试 Redis → 失败 → 降级到本地缓存
```

**Q13：为什么要做 Redis 容错？直接用 Spring Cache 不行吗？**

A：Spring Cache 的抽象层太厚，我需要更细粒度的操作（SETNX、INCR、TTL 检查），Spring Cache 不支持。而且 Spring Cache 没有内置的降级机制——Redis 挂了直接报错。

我的方案：每次操作先尝试 Redis，失败时自动切换到 ConcurrentHashMap 本地缓存，核心业务不中断。

**Q14：ConcurrentHashMap 本地缓存是怎么实现过期的？**

A：用了两个 Map：
```java
ConcurrentHashMap<String, Object> memoryCache;   // 存数据
ConcurrentHashMap<String, Long> expireMap;        // 存过期时间戳
```

读取时检查是否过期（懒惰淘汰）：
```java
Object get(String key) {
    Long expire = expireMap.get(key);
    if (expire != null && System.currentTimeMillis() > expire) {
        memoryCache.remove(key);  // 过期了，删掉
        return null;
    }
    return memoryCache.get(key);
}
```

"懒惰"的意思：不是后台线程定期清理，而是**读的时候才检查**，实现简单且无线程开销。

**Q15：SETNX 在本地缓存里怎么实现线程安全？**

A：用 `synchronized(memoryCache)` 块。虽然性能不如 Redis 的原生原子操作，但本地缓存只是降级方案，流量不会很大，synchronized 足够。

### 开放性问题

**Q16：如果让你重新设计，会怎么做？**

A：可以考虑用 Caffeine 或 Guava Cache 替代手写的 ConcurrentHashMap，它们有更成熟的淘汰策略（LRU、LFU）和更好的并发性能。当前方案胜在简单、零依赖。

---

## 第 3 条：AOP 限流 + 幂等

### 基础概念

**Q17：什么是 AOP？底层原理是什么？**

A：AOP（面向切面编程）是把横切关注点（日志、限流、权限）从业务代码中抽离出来。

底层原理是**动态代理**：
- 目标类实现了接口 → 用 JDK 动态代理（Proxy.newProxyInstance）
- 没有接口 → 用 CGLIB 生成子类

Spring 在运行时生成代理对象，方法执行前后自动插入切面逻辑。你的业务代码完全不知道限流的存在。

**Q18：@RateLimit 的限流算法是什么？有什么优缺点？**

A：**固定窗口计数器**，用 **Redis Lua 脚本**保证原子性。

```
窗口：60 秒，限制：10 次
Key：rate_limit:login:192.168.1.1

第 1 次：INCR → 1 → 放行
第 10 次：INCR → 10 → 放行
第 11 次：INCR → 11 → 拒绝
60 秒后：TTL 到期，重新计数
```

**为什么用 Lua 脚本？** 最初实现是先 `increment` 再 `set TTL`，两步操作不原子——并发下可能 key 永不过期，或者崩溃后 TTL 丢失。改用 Lua 脚本后，INCR + EXPIRE 在 Redis 单线程中原子执行：

```lua
local current = redis.call('INCR', KEYS[1])
if current == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end
return current
```

优点：实现简单、原子操作、性能好
缺点：窗口边界可能突发（比如第 59 秒来了 10 个请求，第 60 秒又来 10 个，2 秒内共 20 个）。滑动窗口能解决，但实现更复杂。

面试时可以主动说：如果面试官问"滑动窗口了解吗"，你可以说了解原理，当前项目用固定窗口够用。

**Q19：幂等校验的 key 是怎么生成的？为什么这样设计？**

A：`idempotent:{方法签名}:{Token的SHA-256哈希}`

- 方法签名：保证不同接口的 key 不同（注册和登录的限流互不影响）
- Token 哈希：保证不同用户的 key 不同（你注册不影响我注册）
- **SHA-256**：Token 是敏感信息，不能明文存 Redis。用 SHA-256 而不是 `hashCode()`，因为 hashCode 是 int（32 位），碰撞概率高，两个不同 Token 可能产生相同 hash，导致误拒合法请求

TTL 自动过期（默认 5 秒），过期后可以再次提交。异常抛出 `BusinessException(CONFLICT)` 而非 RuntimeException，确保 GlobalExceptionHandler 返回 409 而非 500。

**Q20：X-Forwarded-For 是什么？为什么要处理它？**

A：当请求经过 Nginx/CDN 等代理时，`request.getRemoteAddr()` 拿到的是代理的 IP，不是真实用户 IP。X-Forwarded-For 头部记录了真实的客户端 IP 链。

```java
// 优先级：X-Forwarded-For > X-Real-IP > getRemoteAddr()
String ip = request.getHeader("X-Forwarded-For");
if (ip == null) ip = request.getHeader("X-Real-IP");
if (ip == null) ip = request.getRemoteAddr();
```

这样限流才能按真实用户 IP 计数，而不是所有用户共享代理 IP。

### 原理深入

**Q21：AOP 有几种通知类型？你用的哪种？**

A：5 种：
- `@Before`：方法执行前
- `@After`：方法执行后（无论是否异常）
- `@AfterReturning`：方法正常返回后
- `@AfterThrowing`：方法抛异常后
- `@Around`：环绕通知（最强大，可以控制是否执行目标方法）

我的 @RateLimit 和 @Idempotent 都用 `@Around`，因为需要在方法执行前检查，不满足条件时直接抛异常阻止执行。

**Q22：限流注解能用在 Controller 层吗？**

A：可以，但不推荐。更好的做法是用在 Service 层或 Controller 方法上。原因是：
- Controller 层是入口，限流在这里拦截可以减少无效的参数解析开销
- 但如果 Service 层被多个 Controller 调用，限流在 Service 层更合理
- 我的项目限流在 Controller 方法上（如 login、register），因为每个接口的限流策略不同

**Q22.1：Redis Lua 脚本是什么？为什么限流要用它？**

A：Lua 脚本是 Redis 2.6+ 支持的原子执行机制。Redis 是单线程的，Lua 脚本在执行期间不会被其他命令打断，所以多条 Redis 命令组合在一起就成了原子操作。

我的项目中有两处用了 Lua 脚本：

1. **限流计数器**：INCR + EXPIRE 原子执行，防止 key 永不过期
2. **点赞切换**：SISMEMBER + SADD/SREM 原子执行，防止并发重复点赞

```java
// 点赞 Lua 脚本
if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    redis.call('SREM', KEYS[1], ARGV[1])   -- 已点赞 → 取消
    return 1
else
    redis.call('SADD', KEYS[1], ARGV[1])    -- 未点赞 → 点赞
    return 0
end
```

面试时可以对比：不用 Lua 的话，需要 `WATCH` + `MULTI` + `EXEC` 乐观锁，实现更复杂且在高并发下重试率高。

---

## 第 4 条：WebSocket STOMP + RabbitMQ

### 基础概念

**Q23：WebSocket 和 HTTP 有什么区别？**

A：
- HTTP：客户端请求 → 服务器响应（单向，每次都要建立连接）
- WebSocket：建立连接后，双方可以随时发消息（双向，持久连接）

适合场景：实时推送（通知、聊天、股票行情），不需要每次请求都建立新连接。

**Q24：STOMP 是什么？为什么不用原生 WebSocket？**

A：STOMP 是 WebSocket 之上的消息协议，类似 HTTP 之于 TCP。

原生 WebSocket 只提供字节流传输，你需要自己定义消息格式。STOMP 提供了标准的消息结构：
```
SEND
destination:/app/chat
content-type:application/json

{"message": "hello"}
```

好处：有 destination（目的地）概念，Spring 可以自动路由消息到 `@MessageMapping` 方法。

**Q25：SockJS 是什么？什么时候需要它？**

A：SockJS 是 WebSocket 的降级方案。某些老旧浏览器或企业代理不支持 WebSocket，SockJS 自动切换到 HTTP 长轮询。

```java
registry.addEndpoint("/ws").withSockJS();  // 加了 SockJS 兜底
```

你的代码不需要关心，SockJS 在底层自动处理协议降级。

**Q26：TopicExchange 的 routing key 是怎么工作的？**

A：TopicExchange 根据消息的 routing key 把消息路由到匹配的队列。

```
消息 routing key: notification.like
匹配规则：notification.*  →  匹配  →  发到对应队列

队列绑定：
  notification.like 队列 ← routing key: notification.like
  notification.comment 队列 ← routing key: notification.comment
  notification.follow 队列 ← routing key: notification.follow
```

### 项目细节

**Q27：为什么用 RabbitMQ 而不是直接用 WebSocket 推送？**

A：两个用途不同：
- RabbitMQ：**异步解耦**。点赞/评论事件先发到队列，消费者异步处理（发通知、更新计数），发送方不需要等待
- WebSocket：**实时推送**。通知处理完后，通过 WebSocket 推送到用户浏览器

流程：用户点赞 → 发 RabbitMQ 消息 → 消费者处理 → 发 WebSocket 推送 → 用户看到通知

**Q28：持久化队列是什么意思？有什么好处？**

A：队列在 RabbitMQ 重启后仍然存在（元数据 + 消息）。如果不用持久化，RabbitMQ 宕机重启后队列和消息都丢了。

```java
new Queue("notification.like", true)  // 第二个参数 true = 持久化
```

**Q29：@ConditionalOnProperty 是什么？为什么要用它？**

A：Spring Boot 的条件注解，满足条件才创建 Bean。

```java
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
```

好处：RabbitMQ 可以通过配置开关。本地开发或测试环境可以关掉 RabbitMQ（`rabbitmq.enabled: false`），系统照样启动，只是没有实时通知功能。

---

## 第 5 条：测试 + 工程化

### 基础概念

**Q30：单元测试和集成测试有什么区别？**

A：
- 单元测试：测试单个类/方法，外部依赖全部 Mock 掉（数据库、Redis 等），速度快（<2ms/测试）
- 集成测试：启动完整 Spring 上下文，连真实数据库，测试模块间的协作，速度慢

我的项目 55 个测试全是单元测试，用 Mockito Mock 掉所有外部依赖。

**Q31：Mockito 的 @Mock 和 @InjectMocks 是什么？**

A：
- `@Mock`：创建一个假对象（所有方法默认返回 null/0/false）
- `@InjectMocks`：创建真实对象，把所有 @Mock 自动注入进去

```java
@Mock
private UserMapper userMapper;         // 假的 Mapper
@InjectMocks
private UserServiceImpl userService;   // 真实的 Service，内部用假 Mapper

when(userMapper.selectById(1L)).thenReturn(user);  // 设定假数据
userService.getUserInfo(1L);                        // 调用真实方法
```

**Q32：MockMvc 的 standaloneSetup 和@WebMvcTest 有什么区别？**

A：
- `@WebMvcTest`：加载 Spring 上下文，自动配置 MVC 基础设施，需要 `@MockBean`
- `standaloneSetup`：手动构建 MockMvc，不需要 Spring 上下文，纯 Mockito，启动更快

我的项目用 standaloneSetup，因为 `@WebMvcTest` 有编译依赖问题，standaloneSetup 更可靠。

### 项目细节

**Q33：你的测试覆盖了哪些场景？**

A：每个 Service 和 Controller 都测了：
- 正常流程（成功注册、登录、发布文章）
- 异常路径（用户不存在、密码错误、无权操作、重复注册）
- 边界情况（空列表、Token 无效、Redis 缓存命中/未命中）

总共 55 个测试：Service 层 32 个 + Controller 层 23 个。

**Q33.1：你提到了 N+1 消除，具体是怎么做的？**

A：评论模块原来有严重的 N+1 问题。查询文章评论时，每个根评论单独查一次子评论、查一次用户信息，20 条评论 × 10 条回复 = 200+ 次 SQL。

改为 3 次批量查询：
```java
// 1. 批量查所有子评论（1 次 SQL 替代 N 次）
List<Comment> allReplies = commentMapper.selectList(
    replyWrapper.in(Comment::getParentId, rootIds));
Map<Long, List<Comment>> repliesByParent = allReplies.stream()
    .collect(Collectors.groupingBy(Comment::getParentId));

// 2. 批量查所有相关用户（1 次 SQL 替代 2N 次）
Set<Long> allUserIds = ...; // 收集所有 userId + replyUserId
Map<Long, User> userMap = userMapper.selectBatchIds(allUserIds).stream()
    .collect(Collectors.toMap(User::getId, u -> u));

// 3. 批量查点赞状态（1 次 Redis pipeline 替代 N 次）
```

**Q33.2：CommentVO 是什么？为什么不直接返回 Comment Entity？**

A：之前返回 `List<Map<String, Object>>`，key 是字符串，编译器无法检查拼写错误（比如 `map.get("contnt")` 不会报编译错误）。

改为 `CommentVO` DTO 后：
- 编译期类型检查，拼错字段名直接编译失败
- API 契约清晰，前端和后端都能看到明确的字段定义
- Swagger 文档自动生成正确的响应结构

**Q33.3：CounterSyncService 是做什么的？解决了什么问题？**

A：解决 **Redis 计数和 MySQL 计数的一致性**问题。

点赞数、浏览数先在 Redis 里递增（快），然后定时同步回 MySQL（持久化）：

```java
// 1. 写入时标记脏数据
counterSyncService.incrementView(articleId);
// 内部：Redis INCR + 写入 article:dirty:{articleId} 脏标记

// 2. 定时任务每 5 分钟同步
@Scheduled(fixedRate = 300_000)
public void syncToDatabase() {
    Set<String> dirtyKeys = redisUtils.keys("article:dirty:*");
    for (String key : dirtyKeys) {
        Long articleId = parseArticleId(key);
        syncArticleCounters(articleId);  // INCR → DB UPDATE → 清理
    }
}
```

这是**最终一致性**模式：Redis 保证写入速度，MySQL 保证持久性，定时任务保证最终一致。面试时可以对比强一致性（每次写都同步 DB，慢）和最终一致性（批量异步同步，快但短暂不一致）。

**Q34：Docker Compose 编排了哪几个服务？**

A：4 个服务：
1. MySQL 8.0 — 数据库，带健康检查和数据持久化
2. Redis 7 — 缓存
3. RabbitMQ 3 — 消息队列
4. Spring Boot App — 后端应用

服务间通过 Docker 内部网络通信（`mysql:3306`、`redis:6379`），App 依赖 MySQL 和 Redis 的健康检查通过后才启动。

**Q35：manualChunks 是什么？怎么优化的？**

A：Vite 构建优化，手动指定大型第三方库打包到独立文件。

```
优化前：AiChatView.js (955KB) — 业务代码 + 第三方库混在一起
优化后：AiChatView.js (50KB) — 只有业务代码
        vendor-md-editor.js (840KB) — 编辑器库单独
        vendor-marked.js (975KB) — Markdown 渲染库单独
```

好处：用户访问首页时不需要下载编辑器代码，首屏加载更快。

**Q36：12 个环境变量外部化是怎么做的？**

A：application.yml 里用 `${ENV_VAR:default}` 占位符：
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:123456}
jwt:
  secret: ${JWT_SECRET:blog-backend-secret-key}
```

本地开发用默认值，Docker 部署时通过 environment 注入实际值。`.env` 文件加入 `.gitignore` 防止密钥泄露。

---

## 综合/开放性问题

**Q37：这个项目你遇到的最大技术挑战是什么？**

A：（选一个你最有感触的）建议回答 Redis 容错或 RAG 集成。

Redis 容错版本：
> "Redis 容错层的设计。最初 Redis 挂了整个系统就不可用，我设计了 RedisUtils 门面类，内置 ConcurrentHashMap 本地缓存和懒惰 TTL 淘汰。难点是保证 Redis 和本地缓存的一致性，以及 SETNX 在本地的线程安全实现。"

RAG 版本：
> "RAG 管线的调优。向量化模型的选择、分块策略、相似度阈值的调整都很有讲究。一开始分块太小导致检索结果碎片化，调大后回答质量明显提升。"

**Q38：如果让你重新做这个项目，你会改什么？**

A：（展示反思能力）
- 缓存层用 Caffeine 替代手写 ConcurrentHashMap，性能更好
- 限流算法从固定窗口升级为滑动窗口，解决窗口边界突发问题
- 前端加 E2E 测试（Playwright），目前只有后端单元测试
- CI/CD 用 GitHub Actions 自动化构建和测试

**Q39：你对 Java 后端开发的理解是什么？**

A：（展示全局视野）
> "后端开发不只是写 CRUD。我觉得核心是三件事：
> 1. **可靠性**：容错、降级、限流、幂等，保证系统在异常情况下仍可用
> 2. **性能**：缓存、批量查询、异步处理，用合理的技术手段提升吞吐
> 3. **可维护性**：测试覆盖、配置外部化、松耦合设计，让系统能持续演进"

**Q40：你还想学什么技术？**

A：（展示学习意愿，不要说"都行"）
- 消息队列的高级特性（死信队列、消息轨迹、延迟消息）
- 分布式事务（Seata）
- 微服务架构（Spring Cloud）
- 云原生（K8s、Service Mesh）

---

## 速查表：每个 Bullet 的关键词

| Bullet | 必须记住的关键词 |
|--------|----------------|
| RAG | 语义检索、向量化、相似度、Function Calling、增量更新 |
| Redis 容错 | 门面模式、ConcurrentHashMap、懒惰 TTL、自动降级 |
| AOP 限流幂等 | 动态代理、**Lua 脚本原子操作**、固定窗口、SHA-256 哈希、BusinessException(409) |
| WebSocket+MQ | STOMP、SockJS、TopicExchange、持久化队列、异步解耦 |
| 测试+工程化 | Mockito、MockMvc、55 tests、**N+1 批量查询消除**、**CommentVO 类型安全**、**CounterSyncService 最终一致性**、manualChunks |

---

## 简历参考（优化版）

> **技术脉动 AI Blog** — 全栈博客系统 | Spring Boot 2.7 + Vue 3 + MySQL + Redis + RabbitMQ
>
> - 设计并实现 Redis 容错层：封装 RedisUtils 门面类，内置 ConcurrentHashMap + 懒惰 TTL 淘汰的本地缓存，Redis 不可用时自动降级，保障系统在中间件故障时仍可运行
> - 基于 AOP 实现声明式限流（@RateLimit）与幂等校验（@Idempotent）：Redis Lua 脚本保证 INCR+EXPIRE 原子性，固定窗口计数器 + 方法签名/Token SHA-256 哈希去重，支持 X-Forwarded-For 代理 IP 识别，零侵入业务代码
> - 集成 LangChain4j RAG 管线：文章分块 → text-embedding-v2 向量化 → 语义检索，结合 Function Calling 让 AI 主动查询博客数据；通过 Spring ApplicationEvent 实现向量索引增量更新
> - 消息驱动架构：RabbitMQ TopicExchange 解耦点赞/评论/通知事件，3 个持久化队列保障消息可靠性；WebSocket STOMP + SockJS 实现实时推送，支持广播与用户级点对点消息
> - 从零搭建测试体系：55 个单元测试（Mockito Service 层 + MockMvc Controller 层），覆盖正常流程与异常路径；批量查询消除评论 N+1 问题，Redis→MySQL 定时同步保证计数最终一致性，Vite manualChunks 代码分割优化首屏加载
