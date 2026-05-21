<template>
  <div class="flex bg-surface h-[calc(100vh-64px)] max-w-[1440px] mx-auto p-6 gap-6 overflow-hidden">
    <!-- Sidebar -->
    <aside class="w-80 hidden lg:flex flex-col gap-6 bg-surface-card rounded-3xl border border-outline/15 p-6 bento-shadow">
      <div class="flex items-center justify-between px-2">
        <h3 class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-[0.2em]">最近对话</h3>
        <div class="flex items-center gap-1">
          <button
            v-if="activeSessionId"
            class="text-on-surface-variant hover:text-on-surface transition-colors p-1"
            title="清除当前对话历史"
            @click="handleClearHistory"
          >
            <Trash2 :size="14" />
          </button>
          <button
            class="text-on-surface-variant hover:text-on-surface transition-colors p-1"
            @click="newSession"
          >
            <Plus :size="16" />
          </button>
        </div>
      </div>

      <div class="flex flex-col gap-2 flex-grow overflow-y-auto">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="flex items-center gap-3 p-3.5 rounded-2xl transition-all group text-left"
          :class="activeSessionId === session.id
            ? 'bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400'
            : 'text-on-surface-variant hover:bg-surface-subtle'"
          @click="switchSession(session.id)"
        >
          <MessageSquare :size="16" :class="activeSessionId === session.id ? 'text-blue-500' : 'text-on-surface/20'" />
          <span class="flex-1 overflow-hidden text-ellipsis whitespace-nowrap text-sm font-medium">
            {{ session.title }}
          </span>
          <button
            class="opacity-0 group-hover:opacity-100 transition-opacity p-0.5 rounded hover:bg-white/50"
            @click.stop="deleteSession(session.id)"
          >
            <Trash2 :size="14" class="text-on-surface-variant hover:text-red-500" />
          </button>
        </button>
        <div v-if="sessions.length === 0" class="text-sm text-on-surface-variant text-center py-8">
          暂无对话
        </div>
      </div>

      <div class="pt-4 border-t border-outline/15 text-[10px] text-on-surface-variant text-center">
        对话历史保存在浏览器本地
      </div>
    </aside>

    <!-- Main Chat Area -->
    <main class="flex-1 flex flex-col bg-surface-card rounded-3xl border border-outline/15 overflow-hidden relative bento-shadow">
      <!-- Header -->
      <header class="px-4 sm:px-6 md:px-10 py-6 border-b border-outline/15 flex items-center justify-between bg-surface-card/80 backdrop-blur-md z-10">
        <div class="flex flex-col gap-0.5">
          <h1 class="font-display font-bold text-2xl text-on-surface tracking-tight">AI 智能助理</h1>
          <p class="font-sans text-sm text-on-surface-variant">由通义千问驱动，实时流式响应</p>
        </div>
        <button
          v-if="activeMessages.length > 0"
          class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-on-surface-variant hover:text-red-500 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
          @click="clearMessages"
        >
          <Trash2 :size="14" />
          清空对话
        </button>
      </header>

      <!-- Messages -->
      <div ref="messagesRef" class="flex-1 overflow-y-auto px-4 sm:px-6 md:px-10 py-8 scroll-smooth">
        <!-- Empty State with Preset Prompts -->
        <div v-if="activeMessages.length === 0" class="flex-1 flex flex-col items-center justify-center gap-8 py-16 h-full">
          <div class="w-20 h-20 rounded-3xl bg-primary-light/30 flex items-center justify-center text-primary">
            <Bot :size="36" />
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold text-on-surface mb-1">开始对话</h3>
            <p class="text-sm text-on-surface-variant">选择下方模板或直接输入你的技术问题</p>
          </div>

          <!-- Preset Prompt Templates -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 w-full max-w-2xl">
            <button
              v-for="prompt in presetPrompts"
              :key="prompt.title"
              class="flex items-start gap-4 p-5 rounded-2xl border border-outline/15 bg-surface-subtle hover:border-primary/30 hover:bg-primary-light/20 transition-all text-left group"
              @click="usePrompt(prompt)"
            >
              <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                :class="prompt.bgClass">
                <component :is="prompt.icon" :size="20" :class="prompt.iconClass" />
              </div>
              <div class="flex-1 min-w-0">
                <h4 class="text-sm font-semibold text-on-surface mb-1 group-hover:text-primary transition-colors">{{ prompt.title }}</h4>
                <p class="text-xs text-on-surface-variant leading-relaxed line-clamp-2">{{ prompt.subtitle }}</p>
              </div>
            </button>
          </div>
        </div>

        <!-- Message List with TransitionGroup -->
        <TransitionGroup
          v-else
          name="msg"
          tag="div"
          class="flex flex-col gap-6"
          @after-enter="scrollToBottom"
        >
          <div
            v-for="(msg, idx) in activeMessages"
            :key="idx"
            class="flex items-start gap-3"
            :class="msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'"
            :data-streaming="loading && msg.role === 'assistant' && idx === activeMessages.length - 1 ? '' : undefined"
          >
            <!-- Avatar -->
            <div
              class="w-10 h-10 shrink-0 rounded-2xl flex items-center justify-center border transition-all"
              :class="msg.role === 'assistant'
                ? 'bg-primary-light/50 border-primary/10 text-primary'
                : 'bg-on-surface border-on-surface text-surface'"
            >
              <Bot v-if="msg.role === 'assistant'" :size="20" />
              <User v-else :size="20" />
            </div>

            <!-- Bubble -->
            <div
              class="px-5 py-3.5 text-sm leading-relaxed max-w-[75%] rounded-2xl"
              :class="[
                msg.role === 'assistant'
                  ? 'bg-surface-subtle text-on-surface rounded-bl-sm markdown-body'
                  : 'bg-blue-600 text-white rounded-br-sm',
                loading && msg.role === 'assistant' && idx === activeMessages.length - 1 ? 'streaming-bubble' : ''
              ]"
            >
              <template v-if="msg.role === 'assistant'">
                <div v-if="msg.content" v-html="renderMd(msg.content)" class="streaming-content" />
                <span v-else-if="loading && idx === activeMessages.length - 1" class="flex gap-1 py-1">
                  <span class="w-1.5 h-1.5 bg-on-surface-variant rounded-full animate-bounce" style="animation-delay: 0s" />
                  <span class="w-1.5 h-1.5 bg-on-surface-variant rounded-full animate-bounce" style="animation-delay: 0.15s" />
                  <span class="w-1.5 h-1.5 bg-on-surface-variant rounded-full animate-bounce" style="animation-delay: 0.3s" />
                </span>
                <span v-else-if="msg.error" class="text-red-500">{{ msg.error }}</span>
              </template>
              <span v-else>{{ msg.content }}</span>
            </div>
          </div>
        </TransitionGroup>
      </div>

      <!-- Input Area -->
      <div class="p-8 bg-gradient-to-t from-surface-card via-surface-card to-transparent pt-12">
        <div class="max-w-4xl mx-auto">
          <div v-if="activePrompt" class="flex items-center gap-2 mb-3 px-1">
            <span class="text-xs text-on-surface-variant">基于模板：</span>
            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-primary-light/30 text-primary text-xs font-medium">
              {{ activePrompt.title }}
              <X :size="12" class="cursor-pointer hover:text-red-500" @click="activePrompt = null; inputText = ''" />
            </span>
          </div>

          <div class="bg-surface-card border border-outline/15 rounded-[28px] shadow-[0_16px_48px_-12px_rgba(0,0,0,0.08)] focus-within:ring-4 focus-within:ring-primary/5 focus-within:border-primary/20 transition-all flex flex-col overflow-hidden">
            <textarea
              ref="inputRef"
              v-model="inputText"
              class="w-full bg-transparent border-none resize-none px-6 py-5 focus:ring-0 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 h-28"
              :placeholder="loading ? 'AI 正在回复中...' : '输入你的问题... (Ctrl + Enter 发送)'"
              :disabled="loading"
              @keydown.ctrl.enter="handleSend"
              @input="autoResize"
            />
            <div class="flex items-center justify-between px-4 pb-4">
              <div class="flex items-center gap-1">
                <button
                  v-for="prompt in quickPrompts"
                  :key="prompt.label"
                  class="px-3 py-1.5 text-xs font-medium text-on-surface-variant hover:text-primary hover:bg-primary-light/20 rounded-lg transition-all"
                  @click="usePrompt(prompt)"
                >
                  {{ prompt.label }}
                </button>
              </div>
              <button
                class="bg-on-surface text-surface px-5 py-2.5 rounded-2xl flex items-center gap-2 hover:bg-on-surface/90 hover:shadow-lg active:scale-95 transition-all group disabled:opacity-40 disabled:cursor-not-allowed"
                :disabled="!inputText.trim() || loading"
                @click="handleSend"
              >
                <span class="font-display font-bold text-xs uppercase tracking-widest">发送</span>
                <Send :size="16" class="group-hover:translate-x-0.5 transition-transform" />
              </button>
            </div>
          </div>
          <div class="text-center mt-4">
            <span class="font-display font-black text-[10px] text-on-surface-variant/30 uppercase tracking-[0.3em]">
              AI 生成的内容可能不准确，请注意核查。
            </span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.min.css'
import { Bot, User, Send, Plus, Trash2, MessageSquare, X, Code2, FileText, Bug, Lightbulb } from 'lucide-vue-next'
import { toast } from '@/composables/useToast'
import { useCodeCopy } from '@/composables/useCodeCopy'
import { chat, getChatHistory, clearChatHistory } from '@/api/ai'

const { injectCopyButtons } = useCodeCopy()

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch { /* fall through */ }
    }
    try {
      return hljs.highlightAuto(code).value
    } catch {
      return code
    }
  }
})

const inputText = ref('')
const loading = ref(false)
const messagesRef = ref()
const inputRef = ref()
const sessions = ref(loadSessions())
const activeSessionId = ref(null)
const activePrompt = ref(null)

// Derive active messages from the selected session
const activeMessages = computed(() => {
  if (!activeSessionId.value) return []
  const session = sessions.value.find(s => s.id === activeSessionId.value)
  return session?.messages || []
})

const presetPrompts = [
  {
    title: '写技术博客',
    subtitle: '帮你撰写一篇结构清晰的技术文章',
    icon: FileText,
    bgClass: 'bg-blue-100 text-blue-600',
    iconClass: 'text-blue-600',
    prompt: '请帮我写一篇技术博客文章，主题是关于前端性能优化的最佳实践，要求包含实际代码示例和可操作的优化建议。'
  },
  {
    title: '解释代码',
    subtitle: '逐行分析代码逻辑与设计思路',
    icon: Code2,
    bgClass: 'bg-emerald-100 text-emerald-600',
    iconClass: 'text-emerald-600',
    prompt: '请帮我解释下面这段代码的功能和设计思路，包括关键逻辑和潜在的改进点：\n\n```javascript\n// 请在此粘贴你的代码\n```'
  },
  {
    title: '修复 Bug',
    subtitle: '描述问题现象，快速定位根因',
    icon: Bug,
    bgClass: 'bg-red-100 text-red-600',
    iconClass: 'text-red-600',
    prompt: '我在项目中遇到了一个 Bug，请帮我分析可能的原因和修复方案。问题描述：\n\n1. 现象：\n2. 预期行为：\n3. 实际行为：\n4. 相关环境信息：'
  },
  {
    title: '技术方案设计',
    subtitle: '从需求到架构的完整技术方案',
    icon: Lightbulb,
    bgClass: 'bg-amber-100 text-amber-600',
    iconClass: 'text-amber-600',
    prompt: '请帮我设计一个技术方案，需求如下：\n\n1. 业务场景：\n2. 核心功能：\n3. 技术栈偏好：\n4. 性能要求：\n5. 团队规模：\n\n请从架构设计、技术选型、数据流、部署方案等方面给出建议。'
  }
]

const quickPrompts = [
  { label: '代码审查', prompt: '请帮我审查以下代码，找出潜在的性能问题、安全隐患和最佳实践偏差：' },
  { label: '学习路径', prompt: '请为我制定一份系统的学习路径，我想深入学习' }
]

function renderMd(content) {
  if (!content) return ''
  return marked(content)
}

// Re-inject copy buttons when active messages change
watch(() => activeMessages.value.map(m => m.content).join(''), () => {
  nextTick(() => injectCopyButtons())
})

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTo({ top: messagesRef.value.scrollHeight, behavior: 'smooth' })
    }
  })
}

function autoResize() {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
    inputRef.value.style.height = Math.min(inputRef.value.scrollHeight, 120) + 'px'
  }
}

function usePrompt(prompt) {
  activePrompt.value = prompt
  inputText.value = prompt.prompt
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus()
      autoResize()
    }
  })
}

// ─── Session Persistence ────────────────────────────────────

function saveSessions() {
  try {
    localStorage.setItem('ai_sessions', JSON.stringify(sessions.value))
  } catch { /* ignore */ }
}

function loadSessions() {
  try {
    const raw = localStorage.getItem('ai_sessions')
    const data = raw ? JSON.parse(raw) : []
    // Ensure all sessions have a messages array
    return data.map(s => ({ ...s, messages: s.messages || [] }))
  } catch {
    return []
  }
}

function persistCurrentSession() {
  if (!activeSessionId.value) return
  const session = sessions.value.find(s => s.id === activeSessionId.value)
  if (session) {
    // The messages are stored directly in the session's reactive messages array;
    // just persist to localStorage
    saveSessions()
  }
}

// ─── Session Management ─────────────────────────────────────

function newSession() {
  if (loading.value) return
  // Save current session state before creating a new one
  if (activeSessionId.value) {
    saveSessions()
  }
  activeSessionId.value = null
  activePrompt.value = null
  inputText.value = ''
}

function switchSession(sessionId) {
  if (sessionId === activeSessionId.value) return
  // Persist the current session's messages before switching
  if (activeSessionId.value) {
    saveSessions()
  }
  activeSessionId.value = sessionId
  activePrompt.value = null
  inputText.value = ''
  scrollToBottom()
}

function deleteSession(sessionId) {
  sessions.value = sessions.value.filter(s => s.id !== sessionId)
  saveSessions()
  if (activeSessionId.value === sessionId) {
    newSession()
  }
}

// ─── Messages Management ────────────────────────────────────

function pushMessage(msg) {
  if (!activeSessionId.value) return
  const session = sessions.value.find(s => s.id === activeSessionId.value)
  if (session) {
    session.messages.push(msg)
  }
}

function updateLastAssistant(content) {
  if (!activeSessionId.value) return
  const session = sessions.value.find(s => s.id === activeSessionId.value)
  if (session && session.messages.length > 0) {
    const last = session.messages[session.messages.length - 1]
    if (last.role === 'assistant') {
      last.content = content
    }
  }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // Auto-create session if no active session
  if (!activeSessionId.value) {
    const sessionId = Date.now()
    const title = text.slice(0, 30) + (text.length > 30 ? '...' : '')
    sessions.value.unshift({ id: sessionId, title, messages: [] })
    activeSessionId.value = sessionId
  }

  // Push user message into session & persist immediately
  pushMessage({ role: 'user', content: text })
  saveSessions()
  inputText.value = ''
  activePrompt.value = null
  if (inputRef.value) inputRef.value.style.height = 'auto'
  scrollToBottom()

  loading.value = true
  // Push empty assistant message for streaming
  pushMessage({ role: 'assistant', content: '' })
  scrollToBottom()

  chat(text, activeSessionId.value, {
    onStart(data) {
      if (data.sessionId) {
        // Update session id if backend assigned a different one
        const session = sessions.value.find(s => s.id === activeSessionId.value)
        if (session && String(data.sessionId) !== String(activeSessionId.value)) {
          session.id = data.sessionId
          activeSessionId.value = data.sessionId
          saveSessions()
        }
      }
    },
    onMessage(data) {
      if (data.content) {
        // Update the last assistant message
        const session = sessions.value.find(s => s.id === activeSessionId.value)
        if (session && session.messages.length > 0) {
          const last = session.messages[session.messages.length - 1]
          if (last.role === 'assistant') {
            last.content += data.content
          }
        }
        scrollToBottom()
      }
    },
    onComplete() {
      loading.value = false
      saveSessions()
      scrollToBottom()
      nextTick(() => injectCopyButtons())
    },
    onError(err) {
      loading.value = false
      const errorMsg = err.message || '请求失败，请稍后重试'
      const session = sessions.value.find(s => s.id === activeSessionId.value)
      if (session && session.messages.length > 0) {
        const last = session.messages[session.messages.length - 1]
        if (last.role === 'assistant') {
          if (last.content) {
            last.content += `\n\n> ⚠️ ${errorMsg}`
          } else {
            last.error = errorMsg
          }
        }
      }
      saveSessions()
      toast.error(errorMsg)
      scrollToBottom()
    }
  })
}

async function handleClearHistory() {
  if (!activeSessionId.value) return
  try {
    await clearChatHistory(activeSessionId.value)
  } catch { /* proceed to clear locally */ }
  clearMessages()
  toast.success('对话历史已清除')
}

function clearMessages() {
  const session = sessions.value.find(s => s.id === activeSessionId.value)
  if (session) {
    session.messages = []
  }
  activePrompt.value = null
  saveSessions()
}

// ─── Keyboard ───────────────────────────────────────────────

function onKeydown(e) {
  if (e.key === 'Escape' && document.activeElement === inputRef.value) {
    inputRef.value.blur()
  }
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
/* ── Message bubble entrance animation ── */
.msg-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.msg-leave-active {
  transition: all 0.25s ease-in;
}
.msg-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.97);
}
.msg-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ── Stabilize streaming bubble against layout shift ── */
.streaming-bubble {
  contain: layout style;
}
.streaming-content {
  will-change: contents;
}

.streaming-content :deep(img) {
  max-width: 100%;
  height: auto;
}
.streaming-content :deep(pre) {
  min-height: 2.5rem;
}
.streaming-content :deep(pre code) {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
