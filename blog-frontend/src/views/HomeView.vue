<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 md:px-8 pb-20">
    <!-- Hero Section -->
    <header class="relative py-10 sm:py-16 lg:py-20 flex flex-col items-center text-center overflow-hidden">
      <!-- Dot grid background -->
      <div
        class="absolute inset-0 opacity-[0.03] dark:opacity-[0.06] pointer-events-none"
        style="background-image: radial-gradient(circle, #64748b 1px, transparent 1px); background-size: 24px 24px;"
      />
      <!-- Mesh gradient glows -->
      <div class="absolute top-[-30%] left-[-10%] w-[50vw] h-[50vw] rounded-full bg-primary/5 blur-[100px] pointer-events-none" />
      <div class="absolute bottom-[-30%] right-[-10%] w-[50vw] h-[50vw] rounded-full bg-primary-light/10 blur-[100px] pointer-events-none" />

      <h1 class="relative font-display font-bold text-3xl sm:text-5xl md:text-7xl mb-6 max-w-4xl tracking-tighter leading-tight reveal bg-gradient-to-b from-slate-900 via-slate-700 to-slate-500 dark:from-slate-100 dark:via-slate-300 dark:to-slate-400 bg-clip-text text-transparent">
        探索技术的无界之美
      </h1>
      <p class="relative font-sans text-lg md:text-xl text-on-surface-variant max-w-2xl leading-relaxed reveal reveal-delay-1">
        分享前沿架构、AI 技术与优雅的代码实践。在这里，代码不仅仅是指令，更是构建未来世界的艺术。
      </p>
    </header>

    <!-- Search Result Banner -->
    <div v-if="route.query.q" class="mb-10 flex justify-center">
      <div class="inline-flex items-center gap-3 px-5 py-2.5 bg-on-surface text-surface rounded-full text-sm font-medium">
        <Search :size="14" />
        <span>搜索结果: "{{ route.query.q }}"</span>
        <button
          class="ml-1 px-2.5 py-0.5 text-xs font-medium text-slate-400 bg-white/10 dark:bg-white/5 rounded-full hover:bg-white/20 dark:hover:bg-white/10 transition-colors"
          @click="clearSearch"
        >
          清除
        </button>
      </div>
    </div>

    <!-- Filter & Sort Bar -->
    <div class="mb-10 reveal reveal-delay-2">
      <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
        <!-- Multi-Select Category Chips -->
        <div class="flex flex-wrap items-center gap-2">
          <span class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider mr-1">标签</span>
          <button
            v-for="cat in tagOptions"
            :key="cat"
            class="px-4 py-1.5 rounded-full text-xs font-semibold tracking-wide border transition-all duration-200"
            :class="selectedTags.includes(cat)
              ? 'bg-primary text-white border-primary shadow-[0_4px_12px_rgba(0,47,167,0.25)]'
              : 'bg-surface-card text-on-surface-variant border-outline/20 hover:border-primary/40 hover:text-primary'"
            @click="toggleTag(cat)"
          >
            {{ cat }}
          </button>
          <button
            v-if="selectedTags.length > 0"
            class="px-3 py-1.5 rounded-full text-[10px] font-semibold tracking-wide text-on-surface-variant hover:text-red-500 transition-colors"
            @click="clearFilters()"
          >
            清除筛选
          </button>
        </div>

        <!-- Sort Dropdown -->
        <div class="flex items-center gap-2">
          <span class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider">排序</span>
          <div class="relative">
            <select
              :value="sortBy"
              class="appearance-none bg-surface-card border border-outline/20 rounded-xl px-4 py-2 pr-8 text-sm font-medium text-on-surface cursor-pointer outline-none focus:border-primary/40 hover:border-outline/40 transition-colors"
              @change="setSort($event.target.value)"
            >
              <option value="newest">最新发布</option>
              <option value="hottest">最多阅读</option>
            </select>
            <ChevronDown :size="14" class="absolute right-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none" />
          </div>
        </div>
      </div>

      <!-- Active Filter Tags -->
      <div v-if="selectedTags.length > 0 || sortBy === 'hottest'" class="flex items-center gap-2 mt-4 text-xs text-on-surface-variant">
        <span>当前筛选：</span>
        <span
          v-for="t in selectedTags"
          :key="t"
          class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-primary-light/40 text-primary font-medium"
        >
          {{ t }}
          <X :size="12" class="cursor-pointer hover:text-red-500" @click="toggleTag(t)" />
        </span>
        <span v-if="sortBy === 'hottest'" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-amber-50 text-amber-700 font-medium dark:bg-amber-900/30 dark:text-amber-400">
          <Flame :size="12" />
          热门排序
        </span>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <!-- Articles Bento Grid -->
      <div class="lg:col-span-8 grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Main Featured Article -->
        <article
          v-if="bentoArticles.length > 0"
          class="md:col-span-2 bg-surface-card rounded-2xl bento-shadow border border-outline/15 overflow-hidden group cursor-pointer flex flex-col md:flex-row min-h-[320px] card-hover reveal reveal-delay-3"
          @click="goToArticle(bentoArticles[0].id)"
        >
          <div class="md:w-1/2 h-64 md:h-auto relative overflow-hidden bg-surface-subtle">
            <img
              :src="bentoArticles[0].coverImage || getFallbackImage(bentoArticles[0].id, bentoArticles[0].title)"
              :alt="bentoArticles[0].title"
              loading="lazy"
              class="w-full h-full object-cover transition-transform duration-700 ease-in-out group-hover:scale-105"
              @error="(e) => { e.target.style.display = 'none' }"
            />
            <div class="absolute inset-0 bg-slate-900/10 mix-blend-multiply pointer-events-none" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent md:hidden" />
          </div>
          <div class="p-5 sm:p-6 md:p-8 lg:p-10 md:w-1/2 flex flex-col justify-center">
            <div class="flex items-center gap-3 mb-4">
              <span class="bg-surface-subtle text-on-surface-variant px-3 py-1 rounded-full text-[10px] font-semibold tracking-wider font-display uppercase">
                {{ bentoArticles[0].categoryName || '文章' }}
              </span>
              <span class="text-on-surface-variant text-xs flex items-center gap-1">
                <Clock :size="12" />
                {{ formatDate(bentoArticles[0].createTime) }}
              </span>
              <span class="text-on-surface-variant text-xs">{{ estimateReadingTime(bentoArticles[0].content || '') }} 分钟阅读</span>
            </div>
            <!-- Stats Row -->
            <div class="flex items-center gap-3 mb-4 text-xs text-on-surface-variant">
              <span class="inline-flex items-center gap-1"><Eye :size="13" /> {{ formatCount(bentoArticles[0].viewCount) }} 阅读</span>
              <span class="inline-flex items-center gap-1"><ThumbsUp :size="13" /> {{ formatCount(bentoArticles[0].likeCount) }}</span>
              <span class="inline-flex items-center gap-1"><MessageCircle :size="13" /> {{ formatCount(bentoArticles[0].commentCount) }}</span>
            </div>
            <div class="flex items-start justify-between gap-4 mb-4">
              <h2 class="font-display font-bold text-2xl text-on-surface group-hover:text-primary transition-colors leading-tight">
                {{ bentoArticles[0].title }}
              </h2>
              <button
                class="flex-shrink-0 inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium text-primary/70 bg-primary/5 hover:bg-primary/10 hover:text-primary border border-primary/10 hover:border-primary/20 transition-all duration-300"
                @click.stop="showAiPanel = !showAiPanel"
                title="AI 智能摘要"
              >
                <Sparkles :size="13" />
                <span class="hidden sm:inline">AI 摘要</span>
              </button>
            </div>
            <p class="text-on-surface-variant text-sm leading-relaxed line-clamp-3 mb-4">
              {{ bentoArticles[0].summary || '这篇文章探讨了前沿技术与最佳实践...' }}
            </p>
            <!-- Inline AI Copilot for featured article -->
            <div v-if="showAiPanel" class="-mx-5 sm:-mx-6 md:-mx-8 lg:-mx-10 px-5 sm:px-6 md:px-8 lg:px-10 py-4 bg-primary-light/30 border-y border-primary-light/50" @click.stop>
              <AiCopilot :article-title="bentoArticles[0].title" :article-summary="bentoArticles[0].summary || ''" />
            </div>
          </div>
        </article>

        <!-- Smaller Bento Articles -->
        <article
          v-for="(article, i) in bentoArticles.slice(1)"
          :key="article.id"
          class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 overflow-hidden group cursor-pointer flex flex-col card-hover reveal"
          :class="'reveal-delay-' + (4 + i)"
          @click="goToArticle(article.id)"
        >
          <div class="h-48 relative overflow-hidden bg-surface-subtle">
            <img
              :src="article.coverImage || getFallbackImage(article.id, article.title)"
              :alt="article.title"
              loading="lazy"
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
              @error="(e) => { e.target.style.display = 'none' }"
            />
            <div class="absolute inset-0 bg-blue-900/5 pointer-events-none" />
          </div>
          <div class="p-4 sm:p-5 md:p-8 flex flex-col flex-grow">
            <div class="mb-3">
              <span class="bg-surface-subtle text-on-surface-variant px-3 py-1 rounded-full text-[10px] font-semibold tracking-wider font-display uppercase">
                {{ article.categoryName || '文章' }}
              </span>
            </div>
            <h3 class="font-display font-bold text-xl text-on-surface mb-3 group-hover:text-primary transition-colors leading-tight">
              {{ article.title }}
            </h3>
            <div class="flex items-center gap-2.5 mb-3 text-[11px] text-on-surface-variant">
              <span class="inline-flex items-center gap-1"><Eye :size="12" /> {{ formatCount(article.viewCount) }}</span>
              <span class="inline-flex items-center gap-1"><ThumbsUp :size="12" /> {{ formatCount(article.likeCount) }}</span>
              <span class="inline-flex items-center gap-1"><MessageCircle :size="12" /> {{ formatCount(article.commentCount) }}</span>
            </div>
            <p class="text-on-surface-variant text-xs leading-relaxed line-clamp-2 mt-auto">
              {{ article.summary || '这篇文章探讨了前沿技术与最佳实践...' }}
            </p>
          </div>
        </article>

        <!-- Loading Skeleton -->
        <template v-if="loading && articles.length === 0">
          <ArticleSkeleton v-for="n in 3" :key="n" class="md:col-span-2" />
        </template>

        <!-- Regular Article Cards (below bento) -->
        <div v-if="!loading && listArticles.length > 0" class="md:col-span-2 flex flex-col gap-4">
          <div
            v-for="(article, i) in listArticles"
            :key="article.id"
            class="reveal"
            :class="'reveal-delay-' + (5 + i)"
          >
            <ArticleCard :article="article" />
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="!loading && articles.length === 0" class="md:col-span-2 flex flex-col items-center gap-3 py-20">
          <FileText :size="48" class="text-on-surface-variant/30" />
          <p class="text-base font-semibold text-on-surface-variant">暂无文章</p>
          <p class="text-sm text-on-surface-variant">期待你的第一篇创作</p>
        </div>
      </div>

      <!-- Sidebar -->
      <aside class="lg:col-span-4 flex flex-col gap-8">
        <!-- Author Card -->
        <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-5 sm:p-6 md:p-8 card-hover reveal reveal-delay-4">
          <div class="flex items-center gap-4 mb-6">
            <div class="w-16 h-16 rounded-full bg-primary-light/50 overflow-hidden flex-shrink-0 border-2 border-primary/10 flex items-center justify-center text-primary font-display font-bold text-xl">
              T
            </div>
            <div>
              <h4 class="font-display font-bold text-on-surface text-xl">技术脉动</h4>
              <p class="text-on-surface-variant text-xs font-display uppercase tracking-wider mt-0.5">Tech Blog</p>
            </div>
          </div>
          <p class="text-on-surface-variant text-sm leading-relaxed mb-6">
            致力于探索和分享让代码更优雅、系统更健壮的技术实践。
          </p>
        </div>

        <!-- Trending Card -->
        <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-5 sm:p-6 md:p-8 card-hover reveal reveal-delay-5">
          <h4 class="font-display font-bold text-on-surface text-xl mb-6 flex items-center gap-2">
            <TrendingUp :size="20" class="text-primary" />
            热门趋势
          </h4>
          <ul class="flex flex-col gap-5">
            <li
              v-for="(item, i) in hotArticles"
              :key="item.id"
              class="group cursor-pointer"
              @click="goToArticle(item.id)"
            >
              <div class="flex items-start gap-4">
                <span class="text-on-surface/10 font-display font-black text-2xl w-6 leading-none italic">
                  {{ i + 1 }}
                </span>
                <div class="flex-1">
                  <p class="text-on-surface font-medium text-sm group-hover:text-primary transition-colors leading-snug">
                    {{ item.title }}
                  </p>
                  <span class="text-[10px] font-display uppercase tracking-widest text-on-surface-variant font-bold mt-1 inline-block">
                    {{ formatCount(item.viewCount) }} 阅读
                  </span>
                </div>
                <ChevronRight :size="14" class="text-on-surface/15 group-hover:text-primary transition-colors" />
              </div>
            </li>
          </ul>
          <div v-if="hotArticles.length === 0" class="text-sm text-on-surface-variant text-center py-4">
            暂无热门文章
          </div>
        </div>
      </aside>
    </div>

    <!-- Pagination -->
    <div v-if="total > 10" class="flex items-center justify-center gap-4 mt-12">
      <button
        class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
        :disabled="pageNum <= 1"
        @click="goPage(pageNum - 1)"
      >
        <ChevronLeft :size="16" />
        上一页
      </button>
      <span class="text-sm text-on-surface-variant tabular-nums">{{ pageNum }} / {{ totalPages }}</span>
      <button
        class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
        :disabled="pageNum >= totalPages"
        @click="goPage(pageNum + 1)"
      >
        下一页
        <ChevronRight :size="16" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Clock, TrendingUp, ChevronRight, ChevronLeft, FileText, Sparkles, ChevronDown, X, Flame, Eye, ThumbsUp, MessageCircle } from 'lucide-vue-next'
import AiCopilot from '@/components/ai/AiCopilot.vue'
import { getFallbackImage } from '@/utils/fallbackImages'
import { getArticleList } from '@/api/article'
import { searchArticles } from '@/api/search'
import { getHotArticles } from '@/api/hot'
import { getAllTags } from '@/api/tag'
import { formatDate, formatCount, estimateReadingTime } from '@/utils/format'
import { useArticleFilters } from '@/composables/useArticleFilters'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleSkeleton from '@/components/common/ArticleSkeleton.vue'

const route = useRoute()
const router = useRouter()
const { selectedTags, sortBy, pageNum, toggleTag, setSort, setPage, clearFilters } = useArticleFilters()

const articles = ref([])
const hotArticles = ref([])
const showAiPanel = ref(false)
const loading = ref(false)
const total = ref(0)
const tagOptions = ref([])

async function fetchTags() {
  try {
    const tags = await getAllTags()
    tagOptions.value = (tags || []).map(t => t.name).sort()
  } catch {
    tagOptions.value = []
  }
}

const pageSize = 10
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const bentoArticles = computed(() => {
  if (route.query.q || selectedTags.value.length > 0) return []
  return articles.value.slice(0, 3)
})

const listArticles = computed(() => {
  if (route.query.q || selectedTags.value.length > 0) return articles.value
  return articles.value.slice(3)
})

function goToArticle(id) {
  router.push(`/article/${id}`)
}

async function fetchArticles() {
  loading.value = true
  try {
    const hasSearch = !!route.query.q
    const hasTags = selectedTags.value.length > 0

    if (hasSearch || hasTags) {
      // Use search API for keyword/tag filtering
      const params = { pageNum: pageNum.value, pageSize }
      if (route.query.q) params.keyword = route.query.q
      if (hasTags) params.tagName = selectedTags.value[0]
      const data = await searchArticles(params)
      articles.value = data.records || []
      total.value = data.total || 0
    } else {
      // Normal browsing — use article list API
      const data = await getArticleList({ pageNum: pageNum.value, pageSize })
      const records = data.records || []
      total.value = data.total || 0
      articles.value = sortBy.value === 'hottest'
        ? [...records].sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
        : records
    }
  } catch {
    articles.value = []
    total.value = 0
  }
  loading.value = false
}

async function fetchHotArticles() {
  try {
    hotArticles.value = await getHotArticles(5)
  } catch {
    hotArticles.value = []
  }
}

function clearSearch() {
  router.push('/')
}

function goPage(page) {
  setPage(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Re-fetch when filters/sort/page/search change
watch([selectedTags, sortBy, pageNum, () => route.query.q], () => {
  fetchArticles()
})

onMounted(() => {
  fetchArticles()
  fetchHotArticles()
  fetchTags()
})
</script>
