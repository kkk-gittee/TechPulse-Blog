# 技术脉动 Blog — 简历亮点与面试准备

---

## 一、可写入简历的项目描述

### 一句话概括
基于 Spring Boot + Vue 3 的全栈技术博客系统，支持 AI 辅助写作、SSE 流式对话、RBAC 权限控制、Redis 高并发缓存、WebSocket 实时通知。

### 项目描述（适合放在"项目经历"）

**技术脉动 · TechPulse Blog — 全栈个人博客平台**

- **技术栈**：Spring Boot 2.7 + MyBatis-Plus + Redis + RabbitMQ + MySQL | Vue 3 + Pinia + Tailwind CSS v4 + Vite
- **项目周期**：独立开发，前端 13 个页面、后端 12 个 Controller、数据库 10 张表
- **核心工作**：
  - 设计并实现 JWT 双令牌认证体系（AccessToken 24h + RefreshToken 7d），搭配 Axios 拦截器实现无感刷新，支持 RBAC 权限控制
  - 基于 Redis 实现高并发点赞/收藏的原子化操作、计数器缓存同步、IP 级限流（AOP + 自定义注解）、幂等性校验
  - 接入通义千问 API，实现 SSE（Server-Sent Events）流式 AI 对话，配套 Redis 会话历史管理、日调用量限制
  - 使用 RabbitMQ 异步解耦点赞/评论/关注事件，通过 WebSocket STOMP 协议推送实时通知
  - 前端采用 Bento Grid + Glassmorphism 视觉风格，实现暗色模式、Markdown 代码高亮与一键复制、TOC 目录导航、乐观更新（点赞/收藏即时反馈 + API 失败回滚）
  - 基于 md-editor-v3 搭建 Markdown 编辑器，支持 AI 一键生成/续写/润色文章、剪贴板图片粘贴上传、拖拽上传
  - Docker Compose 一键部署（MySQL + Redis + RabbitMQ + App），前后端分离，Vite 代理解决跨域

### 简历关键字堆栈
`Spring Boot` `MyBatis-Plus` `Redis` `RabbitMQ` `WebSocket` `SSE` `JWT` `Vue 3` `Pinia` `Tailwind CSS` `Vite` `Docker` `MySQL` `AOP` `RESTful API`

---

## 二、技术亮点（可展开描述）

### 1. JWT 双令牌认证 + 无感刷新
- **实现**：后端签发 short-lived AccessToken (24h) + long-lived RefreshToken (7d)，前端 Axios 响应拦截器捕获 401 → 队列化并发刷新 → 重放失败请求
- **亮点**：多个并发 401 请求只触发一次 refresh，其余入队等待，避免重复刷新

### 2. SSE 流式 AI 对话
- **实现**：后端 `SseEmitter` 流式推送通义千问 API 响应，前端 `ReadableStream` 消费、打字机效果渲染、Markdown 实时转换
- **亮点**：相比 WebSocket 更轻量（单向推送即足够），兼容 HTTP/1.1，无需额外协议升级

### 3. 乐观更新 (Optimistic UI)
- **实现**：点赞/收藏/评论操作先更新 UI → 发 API → 失败则回滚，提升交互响应速度
- **亮点**：`prevLiked` / `prevCount` 备份 + try/catch 回滚模式，支持评论嵌套多级点赞

### 4. Redis 原子化高并发操作
- **实现**：`SETNX` 实现幂等校验，`INCR`/`DECR` 维护计数器，定时任务同步到 MySQL
- **亮点**：避免数据库行锁竞争，支撑万级 QPS 点赞场景

### 5. 前端架构设计
- **Pinia 状态管理**：user / notification / theme 三个 store，按职责拆分
- **Composables 模式**：useCodeCopy、useSSEWriter、useToc、useTypewriter 等 10+ 可复用逻辑单元
- **Mock 数据层**：ID >= 1000 触发 Mock 降级，前后端开发解耦

### 6. 暗色模式（CSS 变量 + Tailwind 语义化令牌）
- **实现**：`:root` + `.dark` 双套 CSS 变量，`@theme` 注册语义化颜色令牌
- **亮点**：Ctrl+D 一键切换，所有页面统一适配（含 Markdown 代码块、表格、表单）

---

## 三、面试官可能问到的问题

### 基础架构

**Q1: 为什么选择 Spring Boot 2.7 而不是 3.x？**
A: JDK 兼容性考量——Spring Boot 3.x 要求 JDK 17+，项目环境为 JDK 1.8。2.7.x 是 2.x 的最后一个稳定版本，企业级生产验证充分。如需升级，建议先升级 JDK 再迁移到 3.x。

**Q2: 前后端如何通信？跨域怎么解决？**
A: RESTful API（Axios）+ SSE（AI 流式对话）+ WebSocket STOMP（实时通知）。开发环境 Vite proxy 转发 `/api`、`/uploads`、`/ws` 到后端 8080 端口。后端 CORS 配置 `allowedOriginPatterns("*")` + `allowCredentials(true)` 支持跨域携带 Cookie/Token。

**Q3: 为什么不用 Spring Security？**
A: 项目使用自定义 `AuthInterceptor` + JWT 实现认证，粒度更可控。Spring Security 配置较重，对小规模项目过度抽象。但面试时可以说明：**知道 Spring Security 的原理**（Filter Chain、AuthenticationManager、SecurityContextHolder），如果需要集成 OAuth2/OIDC 会优先选择 Spring Security。

### 认证与安全

**Q4: JWT 的优缺点？Token 被盗怎么办？**
A:
- **优点**：无状态、可水平扩展、适合分布式系统
- **缺点**：无法主动失效（服务端不存储）、payload 仅 Base64 编码（非加密）、过期时间固定
- **防盗措施**：HTTPS 传输、HTTP-Only Cookie 存储（本项目 localstorage）、短有效期 AccessToken + 刷新令牌轮换、设置 IP 绑定校验

**Q5: AccessToken 和 RefreshToken 区别？为什么需要两个？**
A: AccessToken 短期有效（24h），直接携带用户身份访问资源；RefreshToken 长期有效（7d），仅用于换取新的 AccessToken。好处：AccessToken 暴露后影响时间短（24h 内自动过期），RefreshToken 使用频率低、暴露面小。

**Q6: 拦截器如何处理未登录用户访问公开接口？**
A: `WebMvcConfig` 的 `excludePathPatterns` 排除了 `/api/user/login`、`/api/article/list/**`、`/api/search/**` 等公开路径。拦截器只对 `/api/**` 下的**受保护路径**校验 Token，公开路径直接放行。

### 数据库与缓存

**Q7: Redis 和 MySQL 如何保证数据一致性？**
A: 采用**旁路缓存 + 定时同步**策略：
- 写操作：先更新 Redis → 异步写 MySQL（或定时批量同步）
- 读操作：查 Redis → miss 查 MySQL → 回写 Redis
- 点赞计数：Redis `INCR`/`DECR` 实时更新，定时任务（每小时）批量同步到 MySQL
- **承认局限**：极端情况下（Redis 宕机）可能丢失未同步的计数增量，可引入 Redis AOF 持久化 + 消息队列保证最终一致性

**Q8: 缓存穿透、击穿、雪崩如何解决？**
A:
- **穿透**：布隆过滤器 + 缓存空值（null 也缓存，TTL 1min）
- **击穿**：热点 key 加互斥锁（`SETNX`）控制单线程回源查 DB
- **雪崩**：TTL 加随机偏移（如 1h ± 10min），避免大量 key 同时过期

**Q9: MyBatis-Plus 如何避免 N+1 查询？**
A:
- 使用 `<association>` / `<collection>` 标签的 `select` 属性实现嵌套查询（但仍是 N+1）
- 更好的方式：手写 JOIN SQL + ResultMap 一次查询加载关联数据
- MyBatis-Plus 的 `LambdaQueryWrapper` 适合简单 CRUD，复杂关联查询建议手写 SQL

### 消息队列

**Q10: RabbitMQ 在项目中具体用来干什么？**
A: 异步解耦社交互动事件（点赞、评论、关注），生产者（Controller）发送事件消息，消费者异步更新通知、触发 WebSocket 推送。好处：削峰填谷、提高接口响应速度、失败可重试。当前环境 RabbitMQ 禁用（`rabbitmq.enabled: false`），通知降级为轮询模式。

**Q11: 消息丢失怎么办？**
A:
- **生产者端**：RabbitMQ 的 Publisher Confirm 机制确认消息到达 Broker
- **Broker 端**：队列/消息持久化（`durable: true`），镜像队列（HA）
- **消费者端**：手动 ACK（`channel.basicAck`），消费成功后再确认，失败重新入队

### SSE & WebSocket

**Q12: SSE 和 WebSocket 的区别？为什么 AI 对话用 SSE？**
A:
| 特性 | SSE | WebSocket |
|------|-----|-----------|
| 方向 | 单向（服务端→客户端） | 双向 |
| 协议 | HTTP/1.1 | 独立协议 ws:// |
| 断线重连 | 浏览器内置 | 需手动实现 |
| 穿透代理 | 天然支持 | 可能被拦截 |

AI 对话只需服务端推送流式文本，客户端发送请求用普通 POST，无需双向通道。SSE 更轻量、实现更简单、代理友好。

**Q13: SSE 流式响应的实现细节？**
A: 后端 `SseEmitter` + 通义千问 HTTP Stream 读取，逐 chunk 发送给前端。前端 `fetch` + `ReadableStream` 的 `getReader()` 消费字节流，使用 `TextDecoder` 解码，通过打字机效果逐字渲染 Markdown。

### 前端

**Q14: 为什么用 Pinia 而不是 Vuex？**
A: Pinia 是 Vuex 5 的官方替代方案。优势：TypeScript 原生支持、无需 `mutations`（直接用 `actions` 修改 state）、模块化无需 `modules` 嵌套、支持 `$patch` 批量更新、体积小（~1KB）。

**Q15: 乐观更新的原理和适用场景？**
A: 用户操作后**先更新 UI 再发 API**，失败则回滚。适用于成功率高的操作（点赞、收藏），不适合扣款、下单等强一致性场景。本项目实现：`prevLiked` 保存旧状态 → UI 即时更新 → API 失败时恢复 `prevLiked`。

**Q16: Tailwind CSS v4 相比 v3 有什么变化？**
A:
- 基于 CSS 的配置（`@theme` 替代 `tailwind.config.js`）
- 原生 CSS 变量驱动，性能更好
- 内置暗色模式支持更灵活
- 类名生成速度更快（Rust 重写引擎）

### 部署与运维

**Q17: Docker Compose 的编排策略？**
A: 四个服务：MySQL (3306)、Redis (6379)、RabbitMQ (5672/15672)、App (8080)。App 依赖 MySQL/Redis 健康检查，通过 `depends_on` + `healthcheck` 控制启动顺序。数据持久化使用 Docker Volume 挂载。

**Q18: 如何保证生产环境安全？**
A:
- API Key 等敏感配置移入环境变量，不入库
- 生产关闭 Swagger/Spring Boot Actuator 暴露端点
- Nginx 反向代理 + HTTPS + 限流
- 文件上传校验 magic bytes，限制上传目录执行权限
- 定期更新依赖，修复 CVE 漏洞

### 开放性问题

**Q19: 如果用户量增长 10 倍，系统瓶颈在哪？**
A:
1. **数据库**：读写分离（主写从读）+ 分库分表（按 userId 哈希）
2. **缓存**：Redis Cluster 分片存储
3. **AI 对话**：异步化（请求入队列 → Worker 消费 → 结果回写 → 客户端轮询/SSE），避免 Tomcat 线程池耗尽
4. **静态资源**：CDN 加速（封面图、头像等）
5. **全文搜索**：引入 Elasticsearch 替代 MySQL `LIKE` 查询

**Q20: 项目中遇到的难点和解决方案？**
A:
1. **并发点赞计数不一致**：使用 Redis `INCR` 原子操作替代 MySQL `UPDATE count = count + 1`
2. **Token 并发刷新**：请求队列化，多个 401 共享同一个 `refreshAccessToken` Promise
3. **AI 流式输出 Markdown 渲染抖动**：`contain: layout style` + `will-change: contents` CSS 隔离重排
4. **移动端编辑页 header 重叠**：两个固定定位 header 的 border 穿透——合并为单一 header

---

## 四、技术面试自测清单

- [ ] 能口述 JWT 的 header.payload.signature 三段结构
- [ ] 能写出 Redis 实现分布式锁的伪代码（`SET lock_key value NX EX 10`）
- [ ] 能解释 `SseEmitter` 和 `DeferredResult` 的区别
- [ ] 能画出系统的架构图（前端→Nginx→后端→DB/Redis/RabbitMQ）
- [ ] 能解释 `@Transactional` 的传播行为和失效场景
- [ ] 能说出 Vue 3 Composition API 和 Options API 的核心差异
- [ ] 能解释 Vite 的 ESM 开发模式和 Rollup 生产构建原理
- [ ] 能写出 SQL 查询"每个分类下阅读量最高的文章"（窗口函数/子查询）
- [ ] 能说出 RabbitMQ 的 Exchange 类型和路由规则
- [ ] 能列出至少 3 种防止表单重复提交的方法（幂等 token、数据库唯一约束、前端防抖 + 按钮 loading）
