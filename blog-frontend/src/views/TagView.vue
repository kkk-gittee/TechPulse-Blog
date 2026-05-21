<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 md:px-8 py-8">
    <div class="mb-8">
      <h1 class="text-xl sm:text-2xl font-bold text-on-surface mb-2">
        <span class="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-primary/10 text-primary text-sm font-medium mb-3">
          <Tag :size="14" />
          {{ tagName }}
        </span>
      </h1>
      <p class="text-on-surface-variant text-sm">共 {{ total }} 篇文章</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <ArticleSkeleton v-for="i in 3" :key="i" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="text-center py-16">
      <Tag :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant mb-4">{{ loadError }}</p>
      <button class="text-sm text-primary hover:underline" @click="fetchArticles">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="articles.length === 0" class="text-center py-16">
      <Tag :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant">该标签下暂无文章</p>
    </div>

    <!-- Article List -->
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
        @page-change="fetchArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Tag } from 'lucide-vue-next'
import { searchArticles } from '@/api/search'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleSkeleton from '@/components/common/ArticleSkeleton.vue'
import Pagination from '@/components/common/Pagination.vue'

const route = useRoute()
const tagName = ref(route.params.name)
const articles = ref([])
const loading = ref(false)
const loadError = ref('')
const pageNum = ref(1)
const total = ref(0)
const pageSize = 10

async function fetchArticles() {
  loading.value = true
  loadError.value = ''
  try {
    const data = await searchArticles({
      tagName: tagName.value,
      pageNum: pageNum.value,
      pageSize
    })
    articles.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

watch(() => route.params.name, (val) => {
  tagName.value = val
  pageNum.value = 1
  fetchArticles()
})

onMounted(() => {
  fetchArticles()
})
</script>
