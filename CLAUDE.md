# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack blog system with a Spring Boot backend (`blog-backend/`) and Vue 3 frontend (`blog-frontend/`).

## Build & Run Commands

### Backend (`blog-backend/`)
```bash
mvn clean package -DskipTests    # Build
mvn spring-boot:run -DskipTests  # Run locally
mvn test                         # Run tests
docker-compose up -d             # Docker deployment from root (MySQL, Redis, RabbitMQ, App)
```

### Frontend (`blog-frontend/`)
```bash
npm install                      # Install dependencies
npm run dev                      # Dev server on port 3000, proxies /api and /ws to :8080
npm run build                    # Production build
```

### Convenience Scripts (root)
- `start.bat` — launches both backend and frontend
- `stop.bat` — kills both processes

**Prerequisites**: JDK 17+, MySQL 8.0+, Redis, RabbitMQ, Maven 3.6+, Node.js 18+

**Runtime notes**:
- RabbitMQ is **disabled** by default (`rabbitmq.enabled: false` in application.yml) — WebSocket notifications won't work without it
- Backend always needs `-DskipTests` because test files have compilation errors
- npm on this machine may need `--cache C:/Users/K1931/AppData/Local/Temp/npm-cache`

## Architecture

### Backend (`blog-backend/` — Spring Boot 2.7.18)

Package `com.blog`:
- **controller/** — REST endpoints under `/api/`
- **service/** + **service/impl/** — Business logic (interface + implementation)
- **mapper/** — MyBatis-Plus mapper interfaces
- **entity/** — Database entities
- **dto/** — Request/response DTOs
- **config/** — Spring config (security, CORS, MyBatis, RabbitMQ, WebSocket)
- **annotation/** + **aspect/** — `@RateLimit` and `@Idempotent` via AOP
- **common/** — `Result<T>` response wrapper, `Constants`, `GlobalExceptionHandler`
- **utils/** — JWT utilities
- **consumer/** — RabbitMQ message consumers

Key patterns:
- All APIs return `Result<T>` with `{code, message, data}`
- JWT auth via `AuthInterceptor`; access token 24h, refresh token 7d; excluded paths in `WebMvcConfig`
- RabbitMQ decouples like/comment/follow events; WebSocket STOMP pushes to clients
- Backend has real AI chat via SSE (Qwen-Turbo with Redis-backed history, daily limit 100)
- Redis used for caching, counters, sorted sets (hot articles), rate limiting, idempotency, AI chat history

Database schema: `blog-backend/src/main/resources/schema.sql`
Tables: `user`, `article`, `comment`, `category`, `tag`, `article_tag`, `follow`, `favorite`, `browse_history`

### Frontend (`blog-frontend/` — Vue 3 + Vite + Tailwind CSS 4)

Key libraries: `@vueuse/core`, `md-editor-v3` (Markdown editor), `marked` (rendering), `highlight.js` (code highlighting), `lucide-vue-next` (icons), `sockjs-client` + `@stomp/stompjs` (WebSocket)

- **api/** — Axios modules per endpoint group (auth, article, comment, search, category, ai)
- **stores/** — Pinia stores (user, notification, theme)
- **router/** — Vue Router with auth guards
- **views/** — HomeView, ArticleDetailView, AiChatView, LoginView, RegisterView, ProfileView, ArticleEditView
- **components/** — article/ (ArticleCard, ArticleTagList), comment/ (CommentList), ai/ (AiCopilot), common/ (ToastProvider, ConfirmDialog), layout/ (AppNavbar, AppFooter, DefaultLayout)
- **layouts/** — DefaultLayout (header+footer), AuthLayout (centered card)
- **composables/** — useWebSocket, useSSEWriter, useKeyboard, useCodeCopy, useToast, useToc, useArticleFilters
- **utils/** — mockData (12 articles + 30+ comments), fallbackImages (Unsplash), format (reading time, dates), storage, debounce, validate
- **styles/** — Tailwind CSS entry point

### Mock Data System

The frontend currently runs on a comprehensive Mock data layer (not backend APIs):
- **Articles**: 12 full Markdown articles with Unsplash cover images via `fallbackImages.js`
- **Comments**: 30+ nested comments
- **Fallback strategy**: Articles with ID >= 1000 skip API calls and use Mock; comment API failures auto-fallback to Mock
- **AI Copilot**: Uses keyword-matched static replies with typewriter effect, not the backend SSE endpoint
- **Trending/categories**: Also Mock data

This means the frontend works standalone without the backend running. When connecting to real APIs, the Mock fallback must be removed or gated.

### Design System

- **Color**: `--color-primary: #002fa7` (light) / `#4f8cff` (dark), `--color-primary-light: #e8f0fe`
- **Font**: `--font-display: "Manrope"` for headings, system sans-serif stack for body
- **Style**: Glassmorphism + Bento Grid, `max-w-7xl`, `rounded-2xl`/`rounded-3xl`, bento-shadow
- **Naming**: Tailwind CSS semantic tokens (`bg-surface-card`, `text-on-surface`, `border-outline/15`)
- **Animations**: `fade-in-up` (entry), `glow-pulse` (breathing glow), `mesh-drift` (background grid), `typewriter` + `blink-caret` (text), all 0.6s ease-out
- **Hover**: `hover:-translate-y-1 hover:shadow-xl`, buttons `active:scale-95`
- **Dark mode**: Ctrl+D toggle, stored in theme Pinia store
- **Icons**: Lucide (`lucide-vue-next`)

### Key Frontend Patterns
- **Optimistic updates**: Likes and favorites update UI immediately, rollback on API failure
- **Skeleton screens, error states, empty states** expected on every page
- **SSE streaming**: useSSEWriter composable for ReadableStream consumption; AiChatView uses Mock typewriter but composable ready for real SSE
- **Code copy**: Hover-to-reveal copy button on code blocks (`useCodeCopy`)
- **Reading time**: Estimated from mixed Chinese/English/code content (`utils/format.js`)
- **Global shortcuts**: Cmd+K search, Escape close panel (`useKeyboard`)
- **Toast**: Pure Vue 3 reactive toast system (useToast composable + ToastProvider)
- **Persistence**: Pinia stores + `utils/storage.js` for localStorage

### Infrastructure (Docker Compose)

Services: MySQL (3306), Redis (6379), RabbitMQ (5672/15672 mgmt), App (8080)

## Configuration

- **Backend**: `blog-backend/src/main/resources/application.yml` — AI API key (Qwen-Turbo), JWT secret, DB/Redis/RabbitMQ connections, AI system prompt, daily rate limits
- **Frontend**: `.env.development` / `.env.production` — `VITE_API_BASE_URL`, `VITE_WS_URL`
- **API docs**: Knife4j Swagger UI at `http://localhost:8080/doc.html`

## Test Users

All passwords are BCrypt-encrypted in the `user` table:

| Username | Password |
|----------|----------|
| admin | admin123 |
| zhangsan | zhangsan |
| lisi | lisi |
| wangwu | wangwu |
| zhaoliu | zhaoliu |
| sunqi | sunqi |
