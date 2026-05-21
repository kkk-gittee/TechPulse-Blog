<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 md:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-xl sm:text-2xl font-bold text-on-surface">通知中心</h1>
      <button
        v-if="notifications.length > 0"
        class="text-sm text-primary hover:underline"
        @click="handleMarkAllRead"
      >
        全部已读
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="animate-pulse flex items-center gap-4 p-4 rounded-xl bg-surface-card">
        <div class="w-10 h-10 rounded-full bg-surface-subtle" />
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-surface-subtle rounded w-3/4" />
          <div class="h-3 bg-surface-subtle rounded w-1/2" />
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="text-center py-16">
      <p class="text-on-surface-variant mb-4">{{ loadError }}</p>
      <button class="text-sm text-primary hover:underline" @click="loadNotifications">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="notifications.length === 0" class="text-center py-16">
      <Bell :size="48" class="mx-auto text-on-surface-variant/30 mb-4" />
      <p class="text-on-surface-variant">暂无通知</p>
    </div>

    <!-- Notification List -->
    <div v-else class="space-y-2">
      <div
        v-for="(item, i) in notifications"
        :key="i"
        class="flex items-start gap-4 p-4 rounded-xl bg-surface-card border border-outline/10 hover:border-outline/20 transition-colors cursor-pointer"
        @click="handleClick(item)"
      >
        <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="iconBg(item.type)">
          <Heart v-if="item.type === 'LIKE'" :size="18" class="text-white" />
          <MessageCircle v-else-if="item.type === 'COMMENT'" :size="18" class="text-white" />
          <UserPlus v-else-if="item.type === 'FOLLOW'" :size="18" class="text-white" />
          <Bell v-else :size="18" class="text-white" />
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm text-on-surface">{{ item.content || getDefaultContent(item) }}</p>
          <p class="text-xs text-on-surface-variant mt-1">{{ formatTime(item.createTime) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Heart, MessageCircle, UserPlus } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const notificationStore = useNotificationStore()
const loading = ref(false)
const loadError = ref('')

const notifications = computed(() => notificationStore.notifications)

function iconBg(type) {
  const map = { LIKE: 'bg-red-500', COMMENT: 'bg-blue-500', FOLLOW: 'bg-green-500' }
  return map[type] || 'bg-surface-subtle'
}

function getDefaultContent(item) {
  const map = { LIKE: '赞了你的文章', COMMENT: '评论了你的文章', FOLLOW: '关注了你' }
  return map[item.type] || '你有一条新通知'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

function handleMarkAllRead() {
  notificationStore.markAllRead()
}

function handleClick(item) {
  if (item.referenceId) {
    router.push(`/article/${item.referenceId}`)
  }
}

function loadNotifications() {
  // Notifications come from WebSocket store, no API call needed
  loadError.value = ''
}

onMounted(() => {
  loadNotifications()
})
</script>
