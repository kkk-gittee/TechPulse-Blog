<template>
  <div class="h-[calc(100vh-64px)] flex bg-surface-card overflow-hidden">
    <!-- Top Header Bar -->
    <div class="fixed top-16 left-0 right-0 h-16 flex items-center justify-between px-4 sm:px-6 md:px-8 z-[60] bg-surface-card border-b border-outline/15">
      <!-- Left: Back + Title -->
      <div class="flex items-center gap-4">
        <button
          class="p-2 hover:bg-surface-subtle rounded-full transition-all group shrink-0"
          @click="handleBack"
        >
          <ArrowLeft :size="20" class="text-on-surface-variant group-hover:-translate-x-1 transition-transform" />
        </button>
        <span class="font-display font-bold text-lg text-on-surface border-l border-outline/20 pl-4 h-6 flex items-center">
          {{ isEdit ? '编辑文章' : '写文章' }}
        </span>
      </div>

      <!-- Right: Actions -->
      <div class="flex items-center gap-4 sm:gap-6">
        <!-- Auto-save indicator -->
        <span
          class="text-xs font-medium transition-all duration-500 hidden sm:flex items-center gap-1.5 whitespace-nowrap"
          :class="draftStatus === 'saved' ? 'text-emerald-500' : draftStatus === 'saving' ? 'text-amber-500' : 'text-on-surface-variant/40'"
        >
          <span v-if="draftStatus === 'saved'" class="w-1.5 h-1.5 rounded-full bg-emerald-400" />
          <span v-else-if="draftStatus === 'saving'" class="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
          {{ draftStatus === 'saved' ? '已保存' : draftStatus === 'saving' ? '保存中...' : '' }}
        </span>

        <!-- Tool Buttons Group -->
        <div class="flex items-center gap-1">
          <button
            class="px-3 py-1.5 text-on-surface-variant hover:text-primary font-medium transition-all text-sm flex items-center gap-1.5 rounded-lg hover:bg-surface-subtle"
            :class="{ 'text-primary bg-primary-light/30 rounded-lg': showAiPanel }"
            @click="showAiPanel = !showAiPanel; showSettings = false"
          >
            <Sparkles :size="15" />
            AI 助手
          </button>
          <button
            class="px-3 py-1.5 text-on-surface-variant hover:text-on-surface font-medium transition-all text-sm rounded-lg hover:bg-surface-subtle hidden sm:inline-flex"
            :class="{ 'text-on-surface bg-surface-subtle rounded-lg': showSettings }"
            @click="showSettings = !showSettings; showAiPanel = false"
          >
            设置
          </button>
        </div>

        <!-- Vertical Divider -->
        <div class="w-px h-5 bg-outline/20 hidden sm:block" />

        <!-- Publish Button -->
        <button
          class="bg-primary text-white px-4 sm:px-5 py-2.5 rounded-xl font-display font-bold text-sm hover:opacity-90 active:scale-95 transition-all shadow-md disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
          :disabled="loading || !form.title.trim()"
          @click="handleSubmit"
        >
          <span v-if="loading" class="inline-flex items-center gap-2">
            <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            处理中
          </span>
          <span v-else>{{ isEdit ? '更新' : '发布' }}</span>
        </button>
      </div>
    </div>

    <!-- Main Editing Area -->
    <main class="flex-1 overflow-y-auto pt-28 pb-20 px-4 md:px-12 max-w-4xl mx-auto w-full">
      <input
        ref="titleRef"
        v-model="form.title"
        class="w-full text-4xl font-display font-bold text-on-surface placeholder:text-on-surface-variant/20 border-none focus:ring-0 outline-none bg-transparent"
        placeholder="请输入吸人眼球的标题..."
        maxlength="100"
      />
      <div v-if="form.title.length > 0" class="text-right text-xs text-on-surface-variant/40 mt-1 mb-6 tabular-nums">
        {{ form.title.length }}/100
      </div>

      <!-- Divider -->
      <div class="h-px bg-surface-subtle my-6" />

      <!-- MdEditor -->
      <div
        ref="editorContainerRef"
        class="editor-container"
        @dragover.prevent="onDragOver"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
        @paste="onEditorPaste"
      >
        <MdEditor
          v-model="form.content"
          :preview="true"
          :toolbarsExclude="['github', 'htmlPreview']"
          :onUploadImg="onUploadImg"
          style="height: calc(100vh - 280px)"
        />
        <!-- Drop overlay -->
        <div
          v-if="dragOver"
          class="absolute inset-0 z-50 flex items-center justify-center bg-primary/10 border-2 border-dashed border-primary rounded-2xl backdrop-blur-sm pointer-events-none"
        >
          <div class="text-center text-primary">
            <Upload :size="40" class="mx-auto mb-2" />
            <p class="font-bold text-sm">释放以插入图片</p>
          </div>
        </div>
      </div>
    </main>

    <!-- AI Writing Assistant Panel -->
    <aside
      v-if="showAiPanel"
      class="w-96 border-l border-outline/15 bg-surface-subtle/30 overflow-y-auto pt-28 pb-20 px-8"
    >
      <div class="flex items-center gap-2 mb-8 text-on-surface flex-none">
        <Sparkles :size="18" class="text-primary" />
        <h3 class="font-display font-bold text-sm uppercase tracking-widest">AI 写作辅助</h3>
      </div>

      <div class="space-y-6">
        <!-- Generate Article -->
        <div class="bg-surface-card rounded-2xl border border-outline/15 p-5 shadow-sm">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-8 h-8 rounded-lg bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
              <FileText :size="16" class="text-purple-600 dark:text-purple-400" />
            </div>
            <div>
              <h4 class="font-semibold text-sm text-on-surface">一键生成文章</h4>
              <p class="text-xs text-on-surface-variant/60">基于标题自动生成全文</p>
            </div>
          </div>
          <button
            class="w-full py-2.5 rounded-xl bg-purple-50 text-purple-700 font-medium text-sm hover:bg-purple-100 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed dark:bg-purple-900/20 dark:text-purple-300 dark:hover:bg-purple-900/30"
            :disabled="aiLoading || !form.title.trim()"
            @click="aiGenerate"
          >
            <span v-if="aiLoading && aiTask === 'generate'" class="inline-flex items-center gap-2">
              <span class="w-3.5 h-3.5 border-2 border-purple-300 border-t-purple-600 rounded-full animate-spin" />
              AI 写作中...
            </span>
            <span v-else>生成全文</span>
          </button>
        </div>

        <!-- Optimize Paragraph -->
        <div class="bg-surface-card rounded-2xl border border-outline/15 p-5 shadow-sm">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-8 h-8 rounded-lg bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
              <Wand2 :size="16" class="text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <h4 class="font-semibold text-sm text-on-surface">优化段落</h4>
              <p class="text-xs text-on-surface-variant/60">润色选中文本的表达</p>
            </div>
            <button
              class="flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium text-primary bg-primary-light/30 rounded-lg hover:bg-primary-light/50 transition-colors"
              title="自动获取编辑器中选中的文本"
              @click="grabSelectedText"
            >
              <MousePointer2 :size="12" />
              选中
            </button>
          </div>
          <textarea
            v-model="aiInput"
            class="w-full bg-surface-subtle border border-outline/15 rounded-xl p-3 text-xs text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none resize-none mb-3"
            placeholder="先在编辑器中选中文本，再点击「选中」按钮自动获取..."
            rows="3"
          />
          <button
            class="w-full py-2.5 rounded-xl bg-blue-50 text-blue-700 font-medium text-sm hover:bg-blue-100 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed dark:bg-blue-900/20 dark:text-blue-300 dark:hover:bg-blue-900/30"
            :disabled="aiLoading || !aiInput.trim()"
            @click="aiOptimize"
          >
            <span v-if="aiLoading && aiTask === 'optimize'" class="inline-flex items-center gap-2">
              <span class="w-3.5 h-3.5 border-2 border-blue-300 border-t-blue-600 rounded-full animate-spin" />
              AI 优化中...
            </span>
            <span v-else>优化段落</span>
          </button>
        </div>

        <!-- Auto Complete -->
        <div class="bg-surface-card rounded-2xl border border-outline/15 p-5 shadow-sm">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-8 h-8 rounded-lg bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
              <TextQuote :size="16" class="text-emerald-600 dark:text-emerald-400" />
            </div>
            <div>
              <h4 class="font-semibold text-sm text-on-surface">自动续写</h4>
              <p class="text-xs text-on-surface-variant/60">AI 从文末继续写作</p>
            </div>
          </div>
          <button
            class="w-full py-2.5 rounded-xl bg-emerald-50 text-emerald-700 font-medium text-sm hover:bg-emerald-100 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed dark:bg-emerald-900/20 dark:text-emerald-300 dark:hover:bg-emerald-900/30"
            :disabled="aiLoading || !form.content.trim()"
            @click="aiComplete"
          >
            <span v-if="aiLoading && aiTask === 'complete'" class="inline-flex items-center gap-2">
              <span class="w-3.5 h-3.5 border-2 border-emerald-300 border-t-emerald-600 rounded-full animate-spin" />
              AI 续写中...
            </span>
            <span v-else>继续写</span>
          </button>
        </div>

        <!-- AI Output Area -->
        <div v-if="aiOutput" class="bg-surface-card rounded-2xl border border-outline/15 p-5 shadow-sm">
          <div class="flex items-center justify-between mb-3">
            <h4 class="font-semibold text-xs text-on-surface-variant uppercase tracking-widest">AI 输出</h4>
            <div class="flex gap-1">
              <button
                class="px-2.5 py-1 text-xs font-medium text-on-surface-variant hover:text-primary hover:bg-primary-light/20 rounded-lg transition-colors"
                @click="aiInsert"
                title="插入到编辑器末尾"
              >
                插入
              </button>
              <button
                class="px-2.5 py-1 text-xs font-medium text-white bg-primary rounded-lg hover:opacity-90 transition-opacity"
                @click="aiReplace"
                :title="aiTask === 'optimize' ? '替换编辑器中的原文' : '替换编辑器全部内容'"
              >
                {{ aiTask === 'optimize' ? '替换原文' : '替换' }}
              </button>
            </div>
          </div>
          <div class="text-sm leading-relaxed text-on-surface whitespace-pre-wrap max-h-64 overflow-y-auto">
            {{ aiOutput }}
          </div>
        </div>
      </div>
    </aside>

    <!-- Settings Panel -->
    <aside
      v-if="showSettings"
      class="w-80 border-l border-outline/15 bg-surface-subtle/30 overflow-y-auto pt-28 pb-20 px-8"
    >
      <div class="flex items-center gap-2 mb-8 text-on-surface flex-none">
        <Settings :size="18" class="text-primary" />
        <h3 class="font-display font-bold text-sm uppercase tracking-widest">文档设置</h3>
      </div>

      <div class="space-y-10">
        <!-- Category -->
        <div class="space-y-3">
          <label class="flex items-center gap-2 font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest pl-1">
            <LayoutGrid :size="12" />
            文章分类
          </label>
          <div class="relative group">
            <select
              v-model="form.categoryId"
              class="w-full bg-surface-card border border-outline/20 rounded-xl py-3 pl-4 pr-10 appearance-none font-sans text-sm text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all cursor-pointer shadow-sm"
            >
              <option :value="null" disabled>选择一个分类...</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
            <ChevronDown :size="16" class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-on-surface-variant/40 group-hover:text-primary transition-colors" />
          </div>
        </div>

        <!-- Tags -->
        <div class="space-y-4">
          <label class="flex items-center gap-2 font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest pl-1">
            <Tag :size="12" />
            标签 (Tags)
          </label>
          <div class="flex flex-wrap gap-2" v-if="tagList.length > 0">
            <span
              v-for="tag in tagList"
              :key="tag"
              class="flex items-center gap-1.5 px-3 py-1.5 bg-primary-light/40 text-primary rounded-lg text-xs font-semibold border border-primary/5 hover:bg-primary-light/60 transition-all cursor-default"
            >
              {{ tag }}
              <button @click="removeTag(tag)" class="hover:text-red-500">
                <X :size="10" :stroke-width="3" />
              </button>
            </span>
          </div>
          <input
            v-model="tagInput"
            class="w-full bg-surface-card border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all shadow-sm"
            placeholder="输入标签并按回车..."
            @keydown.enter="handleAddTag"
          />
        </div>

        <!-- Cover Image -->
        <div class="space-y-3">
          <label class="flex items-center gap-2 font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest pl-1">
            <ImageIcon :size="12" />
            封面图 (Cover)
          </label>
          <div v-if="form.coverImage" class="relative rounded-2xl overflow-hidden mb-3">
            <img :src="form.coverImage" alt="cover" loading="lazy" class="w-full h-32 object-cover" />
            <button
              class="absolute top-2 right-2 w-7 h-7 flex items-center justify-center bg-black/50 text-white rounded-lg hover:bg-black/70 transition-colors"
              @click="form.coverImage = ''"
            >
              <X :size="14" />
            </button>
          </div>
          <div class="flex gap-2">
            <input
              v-model="form.coverImage"
              class="flex-1 bg-surface-card border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all shadow-sm"
              placeholder="粘贴图片 URL..."
            />
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileChange"
            />
            <button
              class="shrink-0 inline-flex items-center gap-1.5 px-4 py-2 bg-surface-subtle border border-outline/20 rounded-xl text-sm text-on-surface-variant hover:bg-surface-card hover:border-outline/30 transition-all"
              @click="fileInput?.click()"
              :disabled="uploading"
            >
              <Upload :size="16" :class="{ 'animate-spin': uploading }" />
              {{ uploading ? '上传中...' : '本地上传' }}
            </button>
          </div>
          <div v-if="uploading" class="h-1 bg-surface-subtle rounded-full overflow-hidden">
            <div class="h-full bg-primary rounded-full transition-all duration-300" :style="{ width: uploadProgress + '%' }" />
          </div>
        </div>

        <!-- Summary -->
        <div class="space-y-3">
          <label class="flex items-center gap-2 font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest pl-1">
            摘要
          </label>
          <textarea
            v-model="form.summary"
            class="w-full bg-surface-card border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all shadow-sm resize-none"
            placeholder="一句话概括文章内容（选填）"
            rows="3"
          />
        </div>
      </div>
    </aside>

    <!-- Draft Restore Dialog -->
    <Transition name="page">
      <div v-if="showRestoreDialog" class="fixed inset-0 z-[200] flex items-center justify-center p-8">
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="showRestoreDialog = false" />
        <div class="relative bg-surface-card rounded-3xl shadow-2xl p-8 max-w-md w-full">
          <div class="w-12 h-12 rounded-2xl bg-amber-100 flex items-center justify-center mb-4">
            <RotateCcw :size="24" class="text-amber-600" />
          </div>
          <h3 class="font-display font-bold text-xl text-on-surface mb-2">发现未保存的草稿</h3>
          <p class="text-sm text-on-surface-variant leading-relaxed mb-6">
            检测到之前有自动保存的草稿「{{ draftTitle }}」，是否恢复继续编辑？
          </p>
          <div class="flex gap-3">
            <button
              class="flex-1 py-3 rounded-xl bg-surface-subtle text-on-surface font-medium text-sm hover:bg-outline/20 transition-colors"
              @click="discardDraft"
            >
              放弃草稿
            </button>
            <button
              class="flex-1 py-3 rounded-xl bg-primary text-white font-bold text-sm hover:opacity-90 transition-opacity"
              @click="restoreDraft"
            >
              恢复编辑
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Error Toast -->
    <Transition name="page">
      <div v-if="showError" class="fixed bottom-8 left-1/2 -translate-x-1/2 px-5 py-3 bg-on-surface text-surface text-sm font-medium rounded-xl shadow-lg z-[200]">
        {{ errorMsg }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {
  ArrowLeft, Settings, LayoutGrid, ChevronDown, Tag, X, ImageIcon, Upload,
  Sparkles, FileText, Wand2, TextQuote, RotateCcw, MousePointer2
} from 'lucide-vue-next'
import { getArticleDetail, createArticle, updateArticle } from '@/api/article'
import { getCategories } from '@/api/category'
import { uploadImage } from '@/api/upload'
import { toast } from '@/composables/useToast'
import { useSSEWriter } from '@/composables/useSSEWriter'
import { debounce } from '@/utils/debounce'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const categories = ref([])
const showSettings = ref(false)
const showAiPanel = ref(false)
const fileInput = ref(null)
const editorContainerRef = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const dragOver = ref(false)
const contentUploading = ref(false)
const showError = ref(false)
const errorMsg = ref('')
const tagInput = ref('')
const titleRef = ref(null)
const hasUnsavedChanges = ref(false)

// Auto-save state
const draftStatus = ref('')
const showRestoreDialog = ref(false)
const draftTitle = ref('')
const DRAFT_KEY_PREFIX = 'article_draft_'
const DRAFT_TS_KEY_PREFIX = 'article_draft_ts_'

// AI assistant state
const aiLoading = ref(false)
const aiInput = ref('')
const aiOutput = ref('')
const aiTask = ref('')
const aiOriginalText = ref('') // Selected text to be replaced on "替换"
const { stream: aiStream, fetchAI } = useSSEWriter()

// ─── Selected text detection ────────────────────────────

/**
 * 从 md-editor-v3 的 CodeMirror 编辑区域获取当前选中的文本。
 * 如果用户没有选中任何文本，返回空字符串。
 */
function getSelectedText() {
  // Try to get selection from CodeMirror instance within md-editor-v3
  const cmElement = document.querySelector('.editor-container .cm-editor .cm-content')
  if (cmElement) {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      // Verify the selection is within the editor
      if (cmElement.contains(range.commonAncestorContainer)) {
        return selection.toString().trim()
      }
    }
  }
  // Fallback: global selection (works if editor has focus)
  const globalSelection = window.getSelection()
  if (globalSelection && globalSelection.rangeCount > 0) {
    const text = globalSelection.toString().trim()
    if (text) return text
  }
  return ''
}

/**
 * 自动获取编辑器选中文字并填充到 AI 优化输入框。
 * 如果没有选中文字，保持当前输入框内容不变。
 */
function grabSelectedText() {
  const selected = getSelectedText()
  if (selected) {
    aiInput.value = selected
    toast.success(`已获取 ${selected.length} 个字符`)
  } else {
    toast.error('请先在编辑器中选中需要优化的文本')
  }
}

const isEdit = computed(() => !!route.params.id)

const draftKey = computed(() => {
  if (isEdit.value) return `${DRAFT_KEY_PREFIX}edit_${route.params.id}`
  return `${DRAFT_KEY_PREFIX}new`
})

const draftTsKey = computed(() => {
  if (isEdit.value) return `${DRAFT_TS_KEY_PREFIX}edit_${route.params.id}`
  return `${DRAFT_TS_KEY_PREFIX}new`
})

const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  categoryId: null,
  tags: ''
})

const tagList = computed(() => {
  if (!form.tags) return []
  return form.tags.split(',').map(t => t.trim()).filter(Boolean)
})

function handleAddTag(e) {
  if (e.key === 'Enter' && tagInput.value.trim()) {
    const newTag = tagInput.value.trim()
    if (!tagList.value.includes(newTag)) {
      form.tags = form.tags ? `${form.tags}, ${newTag}` : newTag
    }
    tagInput.value = ''
  }
}

function removeTag(tag) {
  const tags = tagList.value.filter(t => t !== tag)
  form.tags = tags.join(', ')
}

function flashError(msg) {
  errorMsg.value = msg
  showError.value = true
  setTimeout(() => { showError.value = false }, 2500)
}

// ─── Auto-save ─────────────────────────────────────────────

function saveDraft() {
  try {
    const draft = {
      title: form.title,
      content: form.content,
      summary: form.summary,
      coverImage: form.coverImage,
      categoryId: form.categoryId,
      tags: form.tags
    }
    localStorage.setItem(draftKey.value, JSON.stringify(draft))
    localStorage.setItem(draftTsKey.value, Date.now().toString())
    draftStatus.value = 'saved'
    hasUnsavedChanges.value = false
    setTimeout(() => { if (draftStatus.value === 'saved') draftStatus.value = '' }, 2000)
  } catch { /* storage full or unavailable */ }
}

// 30 秒防抖自动保存：用户停止编辑 30 秒后触发保存
const debouncedSave = debounce(() => {
  draftStatus.value = 'saving'
  nextTick(() => saveDraft())
}, 30000)

function startAutoSave() {
  // Debounced save is triggered via watch on form changes
}

function stopAutoSave() {
  debouncedSave.cancel()
}

function checkDraft() {
  try {
    const raw = localStorage.getItem(draftKey.value)
    if (!raw) return
    const draft = JSON.parse(raw)
    if (draft.title || draft.content) {
      draftTitle.value = draft.title || '(无标题)'
      showRestoreDialog.value = true
    }
  } catch { /* ignore */ }
}

function restoreDraft() {
  try {
    const raw = localStorage.getItem(draftKey.value)
    if (!raw) return
    const draft = JSON.parse(raw)
    form.title = draft.title || ''
    form.content = draft.content || ''
    form.summary = draft.summary || ''
    form.coverImage = draft.coverImage || ''
    form.categoryId = draft.categoryId
    form.tags = draft.tags || ''
    showRestoreDialog.value = false
    toast.success('草稿已恢复')
  } catch { showRestoreDialog.value = false }
}

function discardDraft() {
  try {
    localStorage.removeItem(draftKey.value)
    localStorage.removeItem(draftTsKey.value)
  } catch { /* ignore */ }
  showRestoreDialog.value = false
}

function clearDraft() {
  try {
    localStorage.removeItem(draftKey.value)
    localStorage.removeItem(draftTsKey.value)
  } catch { /* ignore */ }
}

// Track changes → trigger 30s debounced auto-save
watch(
  () => [form.title, form.content, form.summary, form.coverImage, form.categoryId, form.tags],
  () => {
    hasUnsavedChanges.value = true
    draftStatus.value = '' // Clear previous "saved" status
    debouncedSave()
  },
  { deep: true }
)

// ─── Before unload protection ─────────────────────────────

function onBeforeUnload(e) {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

// ─── Back navigation with unsaved warning ─────────────────

function handleBack() {
  if (hasUnsavedChanges.value) {
    saveDraft()
    toast.success('内容已自动保存为草稿')
  }
  router.back()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploading.value = true
  uploadProgress.value = 0
  try {
    const data = await uploadImage(file, (e) => {
      if (e.total) {
        uploadProgress.value = Math.round((e.loaded / e.total) * 100)
      }
    })
    form.coverImage = data.url
    toast.success('封面上传成功')
  } catch (err) {
    toast.error(err.message || '上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    if (fileInput.value) fileInput.value.value = ''
  }
}

// ─── Image upload helpers (for editor content) ────────────

async function uploadSingleImage(file) {
  const data = await uploadImage(file)
  return data.url
}

function insertMarkdownAtCursor(mdText) {
  // Insert newline before and after image for readability
  form.content = (form.content ? form.content + '\n\n' : '') + mdText + '\n'
}

/**
 * md-editor-v3 built-in image upload callback.
 * Called when user clicks the image toolbar button and selects files.
 */
async function onUploadImg(files, callback) {
  const results = []
  for (const file of files) {
    try {
      const url = await uploadSingleImage(file)
      results.push(url)
    } catch {
      results.push('') // md-editor-v3 skips empty strings
    }
  }
  callback(results)
}

function isImageFile(file) {
  return file && file.type.startsWith('image/')
}

function onDragOver(e) {
  const hasImage = [...(e.dataTransfer?.items || [])].some(
    item => item.kind === 'file' && isImageFile(item.getAsFile())
  )
  if (hasImage) {
    dragOver.value = true
  }
}

function onDragLeave() {
  dragOver.value = false
}

async function onDrop(e) {
  dragOver.value = false
  const files = [...(e.dataTransfer?.files || [])].filter(isImageFile)
  if (files.length === 0) {
    // If files are not images, let md-editor-v3 handle text drops etc.
    return
  }

  contentUploading.value = true
  try {
    for (const file of files) {
      const url = await uploadSingleImage(file)
      insertMarkdownAtCursor(`![](${url})`)
    }
    toast.success(`已上传 ${files.length} 张图片`)
  } catch (err) {
    toast.error(err.message || '图片上传失败')
  } finally {
    contentUploading.value = false
  }
}

async function onEditorPaste(e) {
  const items = [...(e.clipboardData?.items || [])]
  const imageItems = items.filter(item => item.kind === 'file' && isImageFile(item.getAsFile()))

  if (imageItems.length === 0) return

  e.preventDefault()
  contentUploading.value = true
  try {
    for (const item of imageItems) {
      const file = item.getAsFile()
      const url = await uploadSingleImage(file)
      insertMarkdownAtCursor(`![](${url})`)
    }
    toast.success(`已上传 ${imageItems.length} 张图片`)
  } catch (err) {
    toast.error(err.message || '图片上传失败')
  } finally {
    contentUploading.value = false
  }
}

// ─── AI Assistant ─────────────────────────────────────────

/**
 * Build a context-rich prompt for the AI writing backend.
 * Designed to produce professional technical article content.
 */
function buildAIPrompt(mode, selectedText = '') {
  const title = form.title || '(无标题)'
  const context = form.content ? form.content.slice(-600) : ''

  const systemPrompt = `你是一位资深技术编辑，为技术博客"技术脉动"撰稿。
写作风格要求：
- 语言专业但不晦涩，面向中高级开发者
- 适当使用比喻和类比帮助理解概念
- 代码示例优先使用 TypeScript/JavaScript
- 结构层次分明（H2 + H3），段落精简，每段不超过4行
- 避免营销用语，聚焦技术本质`

  const prompts = {
    generate: `${systemPrompt}
根据以下标题生成一篇完整的技术博客文章（Markdown 格式）：

标题：《${title}》

要求：
- 包含引言、3-4个核心章节、总结
- 关键技术点配代码示例
- 总长度约600-1000字`,
    optimize: `${systemPrompt}
我正在撰写技术文章《${title}》。
请优化以下段落，增强技术表达的精确性和可读性：

${selectedText}

要求：
- 保持原意不变
- 改善句子节奏，长短句交替
- 口语化表达转为专业书面语
- 只返回优化后的文本，不加解释`,
    complete: `${systemPrompt}
我正在撰写技术文章《${title}》。
以下是文章当前末尾内容：

${context}

请自然续写下一段（约150-300字），衔接上文逻辑，保持语气一致。只返回续写内容。`
  }

  return prompts[mode] || prompts.optimize
}

// Typing simulation (mock fallback when backend unavailable)
function simulateAiTyping(output, onUpdate, onDone) {
  let index = 0
  const chars = [...output]
  const timer = setInterval(() => {
    if (index < chars.length) {
      const chunk = chars.slice(index, index + 3 + Math.floor(Math.random() * 4)).join('')
      index += chunk.length
      onUpdate(chunk)
    } else {
      clearInterval(timer)
      onDone()
    }
  }, 30 + Math.random() * 20)
  return () => clearInterval(timer)
}

function mockArticleContent(title) {
  return `## 引言

${title} 是当前技术领域备受关注的话题之一。在这篇文章中，我将从基础概念出发，结合实际项目经验，深入探讨其核心原理与最佳实践。

## 背景

在过去的几年里，随着前端工程化的不断演进，传统的开发模式已经难以满足现代 Web 应用对性能和可维护性的要求。${title} 的出现为开发者提供了一种全新的思路。

## 核心概念

### 1. 基本原理

${title} 的核心工作机制通过将复杂的业务逻辑拆分为独立的模块单元，每个模块专注于单一职责，从而实现高度解耦。

### 2. 关键特性

- **模块化设计**：将应用拆分为可复用的独立模块
- **高性能**：采用惰性加载与缓存策略，显著减少首屏时间
- **开发体验**：完善的类型推导与热更新机制

## 实践指南

\`\`\`javascript
// 示例代码：基本配置
const config = {
  entry: './src/index.js',
  output: { path: './dist', filename: 'bundle.js' },
  module: { rules: [{ test: /\.js$/, use: 'babel-loader' }] }
}
\`\`\`

## 总结

通过本文的介绍，我们系统地梳理了 ${title} 的核心知识点。在实际项目中，建议从简单场景开始逐步深入。`
}

function mockOptimizedParagraph(input) {
  const variations = [
    input.replace(/我们/g, '开发者').replace(/需要/g, '应当').replace(/可以/g, '能够').trim(),
    '从工程实践的角度来看，' + input.replace(/^./, c => c.toLowerCase()).replace(/。/g, '；此处还需要关注性能与可维护性之间的平衡。'),
    input.split('。').map(s => s.trim()).filter(Boolean).map(s => s + '。').join('\n\n')
  ]
  return variations[Math.floor(Math.random() * variations.length)]
}

function mockCompletion(content) {
  return `\n\n## 进阶思考\n\n基于上述讨论，还可以从以下几个维度进一步深入：\n\n1. **性能优化**：在生产环境中启用 Tree Shaking 和代码分割，将打包体积降低 30% 以上。\n\n2. **错误边界**：合理设计错误处理机制，避免单点故障。\n\n3. **监控与度量**：接入 APM 工具持续跟踪核心性能指标。\n\n\`\`\`javascript\nperformance.mark('feature-loaded')\nconst m = performance.measure('load-time', 'start', 'feature-loaded')\nconsole.log(\`加载耗时: \${m.duration}ms\`)\n\`\`\``
}

async function aiGenerate() {
  if (aiLoading.value || !form.title.trim()) return
  aiLoading.value = true
  aiTask.value = 'generate'
  aiOutput.value = ''
  aiOriginalText.value = ''

  // Try real SSE backend, fall back to mock
  try {
    await fetchAI('generate', { title: form.title })
    aiOutput.value = aiStream.value
  } catch {
    const content = '[演示模式] ' + mockArticleContent(form.title.trim())
    let text = ''
    simulateAiTyping(content,
      (chunk) => { text += chunk; aiOutput.value = text },
      () => { aiLoading.value = false }
    )
    return
  }
  aiLoading.value = false
}

async function aiOptimize() {
  if (aiLoading.value || !aiInput.value.trim()) return

  // Save original text for targeted replacement
  aiOriginalText.value = aiInput.value.trim()
  aiLoading.value = true
  aiTask.value = 'optimize'
  aiOutput.value = ''

  try {
    await fetchAI('optimize', {
      title: form.title,
      selectedText: aiInput.value.trim()
    })
    aiOutput.value = aiStream.value
  } catch {
    const optimized = '[演示模式] ' + mockOptimizedParagraph(aiInput.value.trim())
    let text = ''
    simulateAiTyping(optimized,
      (chunk) => { text += chunk; aiOutput.value = text },
      () => { aiLoading.value = false; aiInput.value = '' }
    )
    return
  }
  aiLoading.value = false
  aiInput.value = ''
}

async function aiComplete() {
  if (aiLoading.value || !form.content.trim()) return
  aiLoading.value = true
  aiTask.value = 'complete'
  aiOutput.value = ''
  aiOriginalText.value = ''

  try {
    await fetchAI('complete', {
      title: form.title,
      currentContent: form.content
    })
    aiOutput.value = aiStream.value
  } catch {
    const completion = '[演示模式] ' + mockCompletion(form.content)
    let text = ''
    simulateAiTyping(completion,
      (chunk) => { text += chunk; aiOutput.value = text },
      () => { aiLoading.value = false }
    )
    return
  }
  aiLoading.value = false
}

function aiInsert() {
  if (!aiOutput.value) return
  form.content = (form.content + '\n\n' + aiOutput.value).trim()
  aiOutput.value = ''
  aiTask.value = ''
  aiOriginalText.value = ''
  toast.success('AI 内容已追加到文末')
}

function aiReplace() {
  if (!aiOutput.value) return

  if (aiTask.value === 'optimize' && aiOriginalText.value) {
    // Targeted replace: swap the original selected text with AI output
    if (form.content.includes(aiOriginalText.value)) {
      form.content = form.content.replace(aiOriginalText.value, aiOutput.value.trim())
      aiOutput.value = ''
      aiTask.value = ''
      aiOriginalText.value = ''
      toast.success('已将原文替换为 AI 优化版本')
      return
    }
  }

  // Fallback: replace full content
  form.content = aiOutput.value
  aiOutput.value = ''
  aiTask.value = ''
  aiOriginalText.value = ''
  toast.success('AI 内容已替换全文')
}

// ─── Data fetching ─────────────────────────────────────────

async function fetchArticle() {
  if (!isEdit.value) return
  const data = await getArticleDetail(route.params.id)
  Object.assign(form, {
    title: data.title,
    content: data.content,
    summary: data.summary || '',
    coverImage: data.coverImage || '',
    categoryId: data.categoryId,
    tags: data.tags || ''
  })
}

async function fetchCategories() {
  categories.value = await getCategories()
}

async function handleSubmit() {
  if (!form.title.trim()) {
    flashError('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    flashError('请输入文章内容')
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await updateArticle(route.params.id, form)
      toast.success('更新成功')
    } else {
      await createArticle(form)
      toast.success('发布成功')
    }
    clearDraft()
    hasUnsavedChanges.value = false
    router.push('/')
  } catch (e) {
    toast.error(e.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// ─── Lifecycle ─────────────────────────────────────────────

onMounted(() => {
  fetchCategories()
  fetchArticle()

  // Only check draft for new article; edits fetch from server
  if (!isEdit.value) {
    checkDraft()
  }

  startAutoSave()
  window.addEventListener('beforeunload', onBeforeUnload)
})

onUnmounted(() => {
  // Flush pending debounced save on leave
  if (hasUnsavedChanges.value) {
    debouncedSave.flush()
  }
  stopAutoSave()
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<style scoped>
.editor-container :deep(.md-editor) {
  border: none !important;
  border-radius: 0 !important;
  font-size: 16px;
  background: transparent !important;
}

.editor-container :deep(.md-editor-toolbar) {
  position: sticky !important;
  top: 120px !important;
  z-index: 40 !important;
  background: rgba(255, 255, 255, 0.72) !important;
  backdrop-filter: blur(16px) saturate(1.5) !important;
  border: none !important;
  border-radius: 9999px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.03) !important;
  padding: 4px 10px !important;
  margin: 0 auto 16px !important;
  width: fit-content !important;
  max-width: 100% !important;
}

.dark .editor-container :deep(.md-editor-toolbar) {
  background: rgba(30, 30, 30, 0.8) !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.05) !important;
}

.editor-container :deep(.md-editor-toolbar-item) {
  width: 30px !important;
  height: 30px !important;
  border-radius: 8px !important;
  color: #71717a !important;
}

.dark .editor-container :deep(.md-editor-toolbar-item) {
  color: #a1a1aa !important;
}

.editor-container :deep(.md-editor-toolbar-item:hover) {
  background: rgba(0, 0, 0, 0.05) !important;
  color: #111 !important;
}

.dark .editor-container :deep(.md-editor-toolbar-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #e4e4e7 !important;
}

.editor-container :deep(.md-editor-content) {
  padding: 24px 0 !important;
  border: none !important;
}

.editor-container :deep(.md-editor-preview) {
  padding: 0 24px !important;
  border: none !important;
  border-left: none !important;
  background: #fafafa !important;
}

.dark .editor-container :deep(.md-editor-preview) {
  background: #18181b !important;
}

.editor-container :deep(.md-editor-input) {
  padding: 0 24px !important;
  background: #fff !important;
}

.dark .editor-container :deep(.md-editor-input) {
  background: #27272a !important;
}

.editor-container :deep(.cm-focused),
.editor-container :deep(.cm-editor) {
  outline: none !important;
}
</style>
