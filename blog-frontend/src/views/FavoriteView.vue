<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 md:px-8 py-8">
    <h1 class="text-xl sm:text-2xl font-bold text-on-surface mb-8">我的收藏</h1>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <ArticleSkeleton v-for="i in 3" :key="i" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="text-center py-16">
      <Heart :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant mb-4">{{ loadError }}</p>
      <button class="text-sm text-primary hover:underline" @click="fetchFavorites">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="articles.length === 0" class="text-center py-16">
      <Heart :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant mb-2">还没有收藏任何文章</p>
      <router-link to="/" class="text-sm text-primary hover:underline">去发现好文章</router-link>
    </div>

    <!-- Article List -->
    <div v-else class="space-y-4">
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Heart } from 'lucide-vue-next'
import { getFavoriteArticles } from '@/api/favorite'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleSkeleton from '@/components/common/ArticleSkeleton.vue'

const articles = ref([])
const loading = ref(false)
const loadError = ref('')

async function fetchFavorites() {
  loading.value = true
  loadError.value = ''
  try {
    articles.value = await getFavoriteArticles()
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>
