<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 md:px-8 py-8">
    <div class="mb-8">
      <h1 class="text-xl sm:text-2xl font-bold text-on-surface mb-2">{{ categoryName || '分类浏览' }}</h1>
      <p class="text-on-surface-variant text-sm">共 {{ total }} 篇文章</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <ArticleSkeleton v-for="i in 3" :key="i" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="text-center py-16">
      <Folder :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant mb-4">{{ loadError }}</p>
      <button class="text-sm text-primary hover:underline" @click="fetchArticles">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="articles.length === 0" class="text-center py-16">
      <Folder :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant">该分类下暂无文章</p>
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
import { Folder } from 'lucide-vue-next'
import { getArticleList } from '@/api/article'
import { getCategory } from '@/api/category'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleSkeleton from '@/components/common/ArticleSkeleton.vue'
import Pagination from '@/components/common/Pagination.vue'

const route = useRoute()
const categoryId = ref(Number(route.params.id))
const categoryName = ref('')
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
    const [catData, articleData] = await Promise.all([
      getCategory(categoryId.value),
      getArticleList({ categoryId: categoryId.value, pageNum: pageNum.value, pageSize })
    ])
    categoryName.value = catData?.name || ''
    articles.value = articleData.records || []
    total.value = articleData.total || 0
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (val) => {
  categoryId.value = Number(val)
  pageNum.value = 1
  fetchArticles()
})

onMounted(() => {
  fetchArticles()
})
</script>
