<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 md:px-8 pb-20">
    <!-- Loading Skeleton -->
    <template v-if="loadingArticle">
      <div class="grid grid-cols-1 lg:grid-cols-[1fr_240px] gap-8">
        <div>
          <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-10 mb-6 animate-pulse">
            <div class="h-8 w-3/4 bg-surface-subtle rounded-lg mb-6" />
            <div class="flex items-center gap-3 mb-8 pb-6 border-b border-outline/15">
              <div class="w-8 h-8 rounded-full bg-surface-subtle" />
              <div class="h-4 w-20 bg-surface-subtle rounded" />
              <div class="h-4 w-24 bg-surface-subtle rounded" />
              <div class="h-4 w-16 bg-surface-subtle rounded" />
            </div>
            <div class="space-y-4">
              <div class="h-4 w-full bg-surface-subtle rounded" />
              <div class="h-4 w-5/6 bg-surface-subtle rounded" />
              <div class="h-4 w-4/6 bg-surface-subtle rounded" />
              <div class="h-4 w-full bg-surface-subtle rounded" />
              <div class="h-4 w-3/4 bg-surface-subtle rounded" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Error State -->
    <div v-else-if="loadError" class="text-center py-20">
      <div class="w-16 h-16 rounded-full bg-red-50 dark:bg-red-900/20 flex items-center justify-center mx-auto mb-4">
        <AlertCircle :size="32" class="text-red-400" />
      </div>
      <h2 class="font-display font-bold text-xl text-on-surface mb-2">加载失败</h2>
      <p class="text-sm text-on-surface-variant mb-6">{{ loadError }}</p>
      <button
        class="px-6 py-2.5 bg-primary text-white rounded-xl font-medium text-sm hover:opacity-90 transition-opacity"
        @click="fetchArticle(); fetchComments()"
      >
        重新加载
      </button>
    </div>

    <template v-else-if="article">
    <!-- Reading Progress Bar -->
    <div class="fixed top-16 left-0 w-full h-[3px] bg-surface-subtle z-40">
      <div
        class="h-full bg-primary transition-[width] duration-150 ease-out rounded-r-full"
        :style="{ width: progressPercent + '%' }"
      />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_240px] gap-8">
      <div>
    <!-- Article Card -->
    <article class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-4 sm:p-6 md:p-8 lg:p-10 mb-6 card-hover">
      <h1 class="font-display font-bold text-3xl text-on-surface tracking-tight mb-6 leading-tight">
        {{ article.title }}
      </h1>

      <!-- Meta -->
      <div class="flex flex-wrap items-center gap-0 mb-8 pb-6 border-b border-outline/15">
        <router-link :to="`/user/${article.userId}`" class="flex items-center gap-2.5 group">
          <div class="w-8 h-8 rounded-full bg-primary-light/50 text-primary flex items-center justify-center text-sm font-semibold">
            {{ article.nickname?.charAt(0) || 'U' }}
          </div>
          <span class="font-medium text-sm text-on-surface group-hover:text-primary transition-colors">
            {{ article.nickname }}
          </span>
        </router-link>
        <span class="w-[3px] h-[3px] rounded-full bg-outline/30 mx-3" />
        <span class="text-sm text-on-surface-variant">{{ formatDate(article.createTime) }}</span>
        <span class="w-[3px] h-[3px] rounded-full bg-outline/30 mx-3" />
        <span class="text-sm text-on-surface-variant">{{ article.viewCount }} 阅读</span>
        <span class="w-[3px] h-[3px] rounded-full bg-outline/30 mx-3" />
        <span class="text-sm text-on-surface-variant">{{ readingTime }} 分钟阅读</span>
        <template v-if="article.categoryName">
          <span class="w-[3px] h-[3px] rounded-full bg-outline/30 mx-3" />
          <span class="inline-flex px-2.5 py-0.5 text-xs font-medium text-primary bg-primary-light/50 rounded-md">
            {{ article.categoryName }}
          </span>
        </template>
      </div>

      <!-- AI Copilot -->
      <div class="mb-6">
        <AiCopilot :article-title="article.title" :article-summary="article.summary || ''" />
      </div>

      <!-- Cover Image -->
      <div v-if="article.coverImage" class="mb-8 rounded-xl overflow-hidden">
        <ProgressiveImage :src="article.coverImage" :alt="article.title" aspect-ratio="21/9" />
      </div>

      <!-- Content -->
      <div class="markdown-body mb-8" v-html="renderedContent" />

      <!-- Tags -->
      <div v-if="article.tags" class="mb-6">
        <ArticleTagList :tags="article.tags" />
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-between pt-6 border-t border-outline/15">
        <div class="flex items-center gap-1">
          <button
            class="inline-flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all active:scale-90"
            :class="article.isLiked ? 'text-primary' : ''"
            @click="handleLike"
          >
            <ThumbsUp
              :size="18"
              :fill="article.isLiked ? 'currentColor' : 'none'"
              :class="article.isLiked ? 'animate-like-pop' : ''"
            />
            <span>{{ article.likeCount }}</span>
          </button>
          <button
            class="inline-flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all active:scale-90"
            :class="article.isFavorited ? 'text-amber-500' : ''"
            @click="handleFavorite"
          >
            <Bookmark
              :size="18"
              :fill="article.isFavorited ? 'currentColor' : 'none'"
            />
            <span>{{ article.favoriteCount || 0 }}</span>
          </button>
          <button class="inline-flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all">
            <MessageCircle :size="18" />
            <span>{{ article.commentCount }} 评论</span>
          </button>
        </div>
        <div v-if="isOwner" class="flex items-center gap-1">
          <button
            class="inline-flex items-center px-3.5 py-2 text-sm font-medium text-on-surface-variant hover:text-primary hover:bg-primary-light/30 rounded-lg transition-all"
            @click="$router.push(`/edit/${article.id}`)"
          >
            编辑
          </button>
          <button
            class="inline-flex items-center px-3.5 py-2 text-sm font-medium text-on-surface-variant hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
            @click="handleDelete"
          >
            删除
          </button>
        </div>
      </div>
    </article>

    <!-- Comment Section -->
    <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-4 sm:p-6 md:p-8 card-hover">
      <CommentList
        :article-id="article.id"
        :comments="comments"
        :current-user-id="userStore.userInfo?.id"
        @refresh="fetchComments"
      />
    </div>
      </div>

      <!-- TOC Sidebar -->
      <aside v-if="headings.length > 0" class="hidden lg:block">
        <nav class="sticky top-20">
          <h4 class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider mb-3">目录</h4>
          <ul class="space-y-0.5">
            <li v-for="h in headings" :key="h.id">
              <a
                @click.prevent="scrollToHeading(h.id)"
                class="block text-sm py-1.5 transition-colors cursor-pointer rounded-r-lg border-l-2"
                :class="activeId === h.id
                  ? 'text-primary font-medium border-primary bg-primary-light/20'
                  : 'text-on-surface-variant hover:text-on-surface border-transparent hover:bg-surface-subtle'"
                :style="{ paddingLeft: h.level === 3 ? '20px' : '12px' }"
              >
                {{ h.text }}
              </a>
            </li>
          </ul>
        </nav>
      </aside>
    </div>

    <ConfirmDialog
      :visible="showDeleteConfirm"
      title="删除文章"
      message="确定要删除这篇文章吗？此操作不可撤销。"
      confirm-text="删除"
      cancel-text="取消"
      type="danger"
      @confirm="confirmDelete"
      @cancel="showDeleteConfirm = false"
    />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import { ThumbsUp, MessageCircle, Bookmark, AlertCircle } from 'lucide-vue-next'
import { getArticleDetail, deleteArticle, toggleLike } from '@/api/article'
import { toggleFavorite } from '@/api/favorite'
import { getComments } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { formatDate, estimateReadingTime } from '@/utils/format'
import { toast } from '@/composables/useToast'
import { useCodeCopy } from '@/composables/useCodeCopy'
import { useToc } from '@/composables/useToc'
import ArticleTagList from '@/components/article/ArticleTagList.vue'
import CommentList from '@/components/comment/CommentList.vue'
import ProgressiveImage from '@/components/common/ProgressiveImage.vue'
import AiCopilot from '@/components/ai/AiCopilot.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const comments = ref([])
const progressPercent = ref(0)
const showDeleteConfirm = ref(false)
const loadingArticle = ref(true)
const loadError = ref('')

function updateProgress() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  progressPercent.value = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0
}

const readingTime = computed(() => estimateReadingTime(article.value?.content || ''))
const { injectCopyButtons } = useCodeCopy()
const { headings, activeId, extractHeadings, scrollToHeading } = useToc()

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content)
})

watch(renderedContent, () => {
  injectCopyButtons()
  extractHeadings()
})

const isOwner = computed(() => {
  return userStore.userInfo?.id === article.value?.userId
})

async function fetchArticle() {
  loadingArticle.value = true
  loadError.value = ''
  try {
    const data = await getArticleDetail(Number(route.params.id))
    if (data) {
      article.value = data
    } else {
      loadError.value = '文章不存在'
    }
  } catch (e) {
    loadError.value = e?.message || '加载文章失败'
  } finally {
    loadingArticle.value = false
  }
}

async function fetchComments() {
  try {
    comments.value = await getComments(route.params.id)
  } catch {
    comments.value = []
  }
}

async function handleLike() {
  const prevLiked = article.value.isLiked
  const prevCount = article.value.likeCount
  article.value.isLiked = !prevLiked
  article.value.likeCount += article.value.isLiked ? 1 : -1

  try {
    const data = await toggleLike(route.params.id)
    article.value.isLiked = data.liked
    article.value.likeCount = prevCount + (data.liked ? 1 : -1)
  } catch {
    // Rollback optimistic update
    article.value.isLiked = prevLiked
    article.value.likeCount = prevCount
    toast.error('操作失败')
  }
}

async function handleFavorite() {
  const prevFavorited = article.value.isFavorited
  const prevCount = article.value.favoriteCount || 0
  article.value.isFavorited = !prevFavorited
  article.value.favoriteCount = prevCount + (article.value.isFavorited ? 1 : -1)

  try {
    const data = await toggleFavorite(route.params.id)
    article.value.isFavorited = data.favorited
    article.value.favoriteCount = prevCount + (data.favorited ? 1 : -1)
    toast.success(data.favorited ? '已收藏' : '已取消收藏')
  } catch {
    article.value.isFavorited = prevFavorited
    article.value.favoriteCount = prevCount
    toast.error('操作失败')
  }
}

async function handleDelete() {
  showDeleteConfirm.value = true
}

async function confirmDelete() {
  try {
    await deleteArticle(route.params.id)
    toast.success('删除成功')
    router.push('/')
  } catch {
    toast.error('删除失败')
  }
}

watch(() => route.params.id, () => {
  fetchArticle()
  fetchComments()
})

onMounted(() => {
  fetchArticle()
  fetchComments()
  window.addEventListener('scroll', updateProgress, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateProgress)
})
</script>
