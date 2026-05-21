<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 md:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-xl sm:text-2xl font-bold text-on-surface">浏览历史</h1>
      <button
        v-if="articles.length > 0"
        class="text-sm text-red-500 hover:underline"
        @click="handleClear"
      >
        清空历史
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <ArticleSkeleton v-for="i in 3" :key="i" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="text-center py-16">
      <Clock :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant mb-4">{{ loadError }}</p>
      <button class="text-sm text-primary hover:underline" @click="fetchHistory">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="articles.length === 0" class="text-center py-16">
      <Clock :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant">暂无浏览记录</p>
    </div>

    <!-- Article List with Pagination -->
    <div v-else class="space-y-4">
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
      <Pagination
        v-model:page-num="pageNum"
        :total="total"
        :page-size="pageSize"
        @page-change="fetchHistory"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Clock } from 'lucide-vue-next'
import { getHistory, clearHistory } from '@/api/history'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleSkeleton from '@/components/common/ArticleSkeleton.vue'
import Pagination from '@/components/common/Pagination.vue'

const articles = ref([])
const loading = ref(false)
const loadError = ref('')
const pageNum = ref(1)
const total = ref(0)
const pageSize = 10

async function fetchHistory() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await getHistory({ pageNum: pageNum.value, pageSize })
    articles.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleClear() {
  try {
    await clearHistory()
    articles.value = []
    total.value = 0
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchHistory()
})
</script>
