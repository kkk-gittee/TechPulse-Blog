<template>
  <div class="relative" @click.stop>
    <!-- TL;DR Button -->
    <button
      v-if="!panelVisible"
      class="group relative inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium text-primary bg-surface-card border border-primary/20 hover:border-primary/40 hover:shadow-[0_0_20px_rgba(0,47,167,0.1)] transition-all duration-300"
      @click="openPanel"
    >
      <span class="absolute inset-0 rounded-xl bg-gradient-to-r from-primary/5 via-indigo-400/5 to-primary/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
      <Sparkles :size="16" class="text-primary group-hover:animate-pulse" />
      <span class="relative">AI 智能摘要</span>
    </button>

    <!-- Expanded Panel -->
    <div
      v-else
      class="bg-blue-50/40 dark:bg-blue-900/20 backdrop-blur-xl border border-blue-100/60 dark:border-blue-800/30 rounded-2xl overflow-hidden shadow-[0_8px_32px_rgba(0,0,0,0.04)] transition-all duration-500"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-3 border-b border-blue-100/50 dark:border-blue-800/30">
        <div class="flex items-center gap-2 text-sm font-medium text-primary">
          <Sparkles :size="14" />
          <span>AI Copilot</span>
          <span v-if="summaryTyping || qaAnswerTyping" class="w-1.5 h-4 bg-primary/50 animate-pulse rounded-full" />
        </div>
        <button
          class="p-1.5 text-on-surface-variant hover:text-on-surface hover:bg-surface-card/60 rounded-lg transition-colors"
          @click="closePanel"
        >
          <X :size="16" />
        </button>
      </div>

      <!-- Content Area -->
      <div class="px-5 py-4">
        <!-- Summary -->
        <div v-if="summaryText" class="mb-4">
          <p class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider mb-2">TL;DR</p>
          <p class="text-sm text-on-surface leading-relaxed whitespace-pre-wrap">
            {{ summaryText }}<span v-if="summaryTyping" class="inline-block w-[2px] h-4 bg-primary/60 animate-pulse align-text-bottom ml-0.5" />
          </p>
        </div>

        <!-- Q&A Thread -->
        <div v-for="(qa, idx) in qaList" :key="idx" class="mb-4">
          <p class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider mb-1.5">
            <Sparkles :size="10" class="inline mr-1 text-primary" />
            {{ qa.question }}
          </p>
          <p class="text-sm text-on-surface-variant leading-relaxed whitespace-pre-wrap pl-4 border-l-2 border-blue-100">
            {{ qa.answer }}<span v-if="qaAnswerTyping && idx === qaList.length - 1" class="inline-block w-[2px] h-4 bg-primary/60 animate-pulse align-text-bottom ml-0.5" />
          </p>
        </div>
      </div>

      <!-- Follow-up Input -->
      <div class="px-5 py-3 border-t border-blue-100/50 flex items-center gap-2">
        <input
          v-model="question"
          class="flex-1 bg-surface-card/60 border border-outline/10 rounded-xl px-3.5 py-2 text-xs text-on-surface placeholder:text-on-surface-variant focus:ring-2 focus:ring-primary/10 focus:border-primary/30 outline-none transition-all"
          placeholder="向 AI 提问关于本文的技术细节..."
          :disabled="summaryTyping || qaAnswerTyping"
          @keyup.enter="askQuestion"
        />
        <button
          class="flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-xl bg-primary text-white hover:opacity-90 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
          :disabled="!question.trim() || summaryTyping || qaAnswerTyping"
          @click="askQuestion"
        >
          <ArrowUp :size="14" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Sparkles, X, ArrowUp } from 'lucide-vue-next'
import { chat } from '@/api/ai'

const props = defineProps({
  articleTitle: { type: String, default: '' },
  articleSummary: { type: String, default: '' }
})

const panelVisible = ref(false)
const summaryText = ref('')
const summaryTyping = ref(false)
const qaAnswerTyping = ref(false)
const qaList = ref([])
const question = ref('')

function openPanel() {
  panelVisible.value = true
  runSummary()
}

function closePanel() {
  panelVisible.value = false
  summaryText.value = ''
  qaList.value = []
  question.value = ''
}

onMounted(() => window.addEventListener('close-panels', closePanel))
onUnmounted(() => window.removeEventListener('close-panels', closePanel))

function runSummary() {
  summaryTyping.value = true
  summaryText.value = ''

  const prompt = `请用 2-3 句话概括以下文章的核心内容，输出纯文本，不要 Markdown 格式：\n\n标题：${props.articleTitle}\n摘要：${props.articleSummary || '暂无摘要'}`

  chat(prompt, null, {
    onMessage(data) {
      if (data?.content) summaryText.value += data.content
    },
    onComplete() {
      summaryTyping.value = false
    },
    onError() {
      summaryText.value = '摘要生成失败，请稍后重试'
      summaryTyping.value = false
    }
  })
}

function askQuestion() {
  const q = question.value.trim()
  if (!q) return
  question.value = ''
  const entry = { question: q, answer: '' }
  qaList.value.push(entry)
  qaAnswerTyping.value = true

  const prompt = `你是一位技术博客助手。用户正在阅读一篇文章《${props.articleTitle}》。${props.articleSummary ? `文章摘要：${props.articleSummary}` : ''}\n\n用户提问：${q}\n\n请基于文章上下文简要回答，输出纯文本。`

  chat(prompt, null, {
    onMessage(data) {
      if (data?.content) {
        const last = qaList.value[qaList.value.length - 1]
        if (last) last.answer += data.content
      }
    },
    onComplete() {
      qaAnswerTyping.value = false
    },
    onError() {
      const last = qaList.value[qaList.value.length - 1]
      if (last) last.answer = '回答生成失败，请稍后重试'
      qaAnswerTyping.value = false
    }
  })
}
</script>
