# 项目交接文档：技术脉动 AI Blog

## 当前技术栈

- **前端框架**: Vue 3 (Composition API + `<script setup>`)
- **构建工具**: Vite 5
- **CSS 方案**: Tailwind CSS v4（`@theme` 自定义令牌）
- **图标库**: lucide-vue-next
- **状态管理**: Pinia
- **路由**: Vue Router 4（auth guards）
- **Markdown 渲染**: marked
- **HTTP 客户端**: Axios（拦截器统一处理 `Result<T>` 响应）
- **后端**: Spring Boot 2.7.18（MyBatis-Plus、RabbitMQ、Redis、WebSocket STOMP）
- **包管理**: npm

---

## 核心文件结构

```
src/
├── views/
│   ├── HomeView.vue              # 首页：Hero + Bento Grid + 侧边栏 + AI Copilot 内嵌
│   ├── ArticleDetailView.vue     # 文章详情：正文 + 评论区 + AI Copilot
│   ├── AiChatView.vue            # AI 助手独立页：对话面板 + 侧边栏 + Mock 流式回复
│   ├── LoginView.vue             # 登录页
│   ├── RegisterView.vue          # 注册页
│   ├── ProfileView.vue           # 个人中心
│   └── ArticleEditView.vue       # 文章编辑器（Markdown）
├── components/
│   ├── article/
│   │   ├── ArticleCard.vue       # 列表卡片组件
│   │   ├── BentoCard.vue         # Bento 卡片变体
│   │   └── ArticleTagList.vue    # 标签列表
│   ├── comment/
│   │   └── CommentList.vue       # 评论列表（支持嵌套回复）
│   ├── ai/
│   │   └── AiCopilot.vue         # 文章页内嵌 AI 摘要 + 问答组件
│   ├── layout/
│   │   ├── AppNavbar.vue         # 顶部导航（搜索框、通知铃铛、用户下拉）
│   │   ├── AppFooter.vue         # 页脚
│   │   └── DefaultLayout.vue     # 默认布局容器
│   └── common/
│       └── ToastProvider.vue     # 全局 Toast 通知
├── composables/
│   ├── useTypewriter.js          # 打字机效果（setTimeout 递归，25ms/字）
│   ├── useToast.js               # 响应式 Toast 系统
│   ├── useWebSocket.js           # WebSocket STOMP 封装
│   └── useSSE.js                 # SSE 流式请求封装
├── api/
│   ├── request.js                # Axios 实例 + 拦截器
│   ├── article.js                # 文章 CRUD + 点赞
│   ├── comment.js                # 评论接口
│   ├── auth.js                   # 登录/注册
│   ├── search.js                 # 搜索
│   ├── category.js               # 分类
│   └── ai.js                     # AI 聊天 SSE 接口（已废弃，改用 Mock）
├── stores/
│   ├── user.js                   # 用户状态（登录信息、token）
│   └── notification.js           # 通知状态
├── utils/
│   ├── mockData.js               # 12 篇完整 Mock 文章 + 30+ 评论 + 热门列表
│   ├── fallbackImages.js         # 8 张 Unsplash 高质量封面图 + 哈希选择器
│   └── format.js                 # 日期/数字格式化工具
├── router/
│   └── index.js                  # 路由配置（auth meta 守卫）
└── styles/
    └── global.css                # Tailwind @theme 令牌、动画关键帧、.reveal 工具类
```

---

## 已完成的核心功能

- **Bento Grid 首页布局** — 主推文章大卡片 + 2 个小 Bento 卡片 + 列表卡片 + 侧边栏
- **完整的 Mock 数据系统** — 12 篇专业文章（含完整 Markdown 正文），30+ 嵌套评论，热门趋势列表，Unsplash 封面图
- **Mock 降级策略** — ID >= 1000 的文章跳过 API 直接走 Mock；评论 API 失败自动回退 Mock
- **AI Copilot 内嵌组件** — 文章页/首页均可展开 TL;DR 摘要（打字机流式输出）+ 追问 Q&A
- **首页视觉质感** — 圆点网格背景、渐变光晕、交错入场动画（`.reveal` + `.reveal-delay-N`）
- **文章详情页** — Markdown 渲染、封面图、标签、点赞/评论操作栏、作者信息、编辑/删除（仅作者可见）
- **搜索功能** — 搜索词横幅 + 清除按钮
- **分类筛选** — 全部/按分类切换
- **用户认证** — 登录/注册、路由守卫、Pinia 持久化
- **Toast 通知系统** — 纯 Vue 响应式实现，替代 Element Plus
- **图片优雅降级** — `@error` 处理器 + 灰色背景占位 + `getFallbackImage()` 哈希选择

---

## 当前正在解决的问题

**AiChatView.vue 修复**：
- ✅ 头像与气泡对齐错乱 → 改用 `flex items-start gap-3`，头像 `w-10 h-10 shrink-0`
- ✅ 用户气泡样式 → `bg-blue-600 text-white rounded-br-sm max-w-[75%]`
- ✅ AI 气泡样式 → `bg-slate-100 text-slate-800 rounded-bl-sm max-w-[75%]`
- ✅ Mock 流式回复 → 推入空 assistant 消息（显示跳动的三个点）→ 400-800ms 延迟 → 打字机逐字输出（~25ms/字）
- ✅ 关键词匹配 7 个主题回复（React vs Vue、性能优化、Docker、CI/CD、架构、TypeScript、问候）
- ✅ 模板字符串转义修复 — 内部反引号和 `${}` 冲突已解决

**已知局限性**：
- AI 回复为静态 Mock，未接入真实 LLM 后端
- 首页文章始终使用 Mock 数据，未对接后端
- 热门趋势、分类列表均使用 Mock 数据
- 评论功能仅为展示，提交评论未落地

---

## 全局设计规范

- **风格**: 亮色系 + 毛玻璃 (Glassmorphism) + Bento 卡片
- **主色调**: `--color-primary: #002fa7`（深蓝），`--color-primary-light: #e8f0fe`（浅蓝背景）
- **字体**: `--font-display: "Manrope"`（标题），`--font-sans` 为系统无衬线栈
- **卡片**: `rounded-2xl` / `rounded-3xl`，`bento-shadow`（柔和阴影），边框 `border-slate-100`
- **动画**: `fade-in-up`（入场）、`glow-pulse`（光晕呼吸）、`mesh-drift`（网格漂移）、`typewriter`（打字）、`blink-caret`（闪烁光标），统一 `0.6s ease-out`
- **交互动效**: 卡片悬停 `hover:-translate-y-1 hover:shadow-xl`，按钮 `active:scale-95`
- **圆角体系**: 按钮 `rounded-xl`/`rounded-2xl`，气泡 `rounded-2xl` + 朝向角直角（`rounded-br-sm` / `rounded-bl-sm`）
- **背景**: 页面底 `bg-slate-50/50`，卡片 `bg-white`，毛玻璃 `backdrop-blur-md`
