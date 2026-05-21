<template>
  <div
    class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 overflow-hidden group cursor-pointer flex card-hover"
    @click="$router.push(`/article/${article.id}`)"
  >
    <div class="flex-1 p-6 flex flex-col min-w-0">
      <h3 class="font-display font-bold text-lg text-on-surface mb-2 group-hover:text-primary transition-colors leading-tight line-clamp-2">
        {{ article.title }}
      </h3>
      <p class="text-on-surface-variant text-sm leading-relaxed line-clamp-2 mb-4">
        {{ article.summary || '这是一篇精彩的文章，点击阅读全文了解更多信息...' }}
      </p>
      <div class="flex items-center gap-0 text-xs text-on-surface-variant mt-auto flex-wrap">
        <span class="inline-flex items-center gap-1.5 font-medium text-on-surface-variant">
          <span class="w-1.5 h-1.5 rounded-full bg-primary" />
          {{ article.nickname }}
        </span>
        <span class="mx-2 opacity-40">&middot;</span>
        <span>{{ formatDate(article.createTime) }}</span>
        <span class="mx-2 opacity-40">&middot;</span>
        <span class="inline-flex items-center gap-1">
          <Eye :size="12" />
          {{ formatCount(article.viewCount) }}
        </span>
        <span class="mx-2 opacity-40">&middot;</span>
        <span class="inline-flex items-center gap-1">
          <ThumbsUp :size="12" />
          {{ formatCount(article.likeCount) }}
        </span>
        <span class="mx-2 opacity-40">&middot;</span>
        <span class="inline-flex items-center gap-1">
          <MessageCircle :size="12" />
          {{ formatCount(article.commentCount) }}
        </span>
        <span class="mx-2 opacity-40">&middot;</span>
        <span>{{ readingTime }} 分钟阅读</span>
      </div>
    </div>
    <div class="w-24 sm:w-36 md:w-44 h-auto flex-shrink-0 relative overflow-hidden bg-surface-subtle">
      <img
        :src="article.coverImage || getFallbackImage(article.id, article.title)"
        :alt="article.title"
        loading="lazy"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
        @error="(e) => { e.target.style.display = 'none' }"
      />
      <div class="absolute inset-0 bg-blue-900/5 pointer-events-none" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ThumbsUp, MessageCircle, Eye } from 'lucide-vue-next'
import { formatDate, formatCount, estimateReadingTime } from '@/utils/format'
import { getFallbackImage } from '@/utils/fallbackImages'

const props = defineProps({ article: { type: Object, required: true } })
const readingTime = computed(() => estimateReadingTime(props.article.content || ''))
</script>
