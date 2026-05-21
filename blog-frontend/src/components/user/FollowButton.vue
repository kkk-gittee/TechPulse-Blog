<template>
  <button
    class="px-5 py-2 rounded-full text-sm font-semibold transition-all"
    :class="isFollowed
      ? 'bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:hover:bg-slate-700'
      : 'bg-primary text-white hover:opacity-90 active:scale-95 shadow-md'"
    :disabled="loading"
    @click="handleToggle"
  >
    <span v-if="loading" class="inline-flex items-center gap-1.5">
      <span class="w-3.5 h-3.5 border-2 border-current/30 border-t-current rounded-full animate-spin" />
    </span>
    <span v-else>{{ isFollowed ? '已关注' : '关注' }}</span>
  </button>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { toggleFollow, checkFollow } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import { toast } from '@/composables/useToast'

const props = defineProps({
  userId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const isFollowed = ref(false)
const loading = ref(false)

async function fetchStatus() {
  if (!userStore.isLoggedIn || !props.userId) return
  try {
    const data = await checkFollow(props.userId)
    isFollowed.value = data.followed
  } catch { /* ignore */ }
}

async function handleToggle() {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  loading.value = true
  try {
    const data = await toggleFollow(props.userId)
    isFollowed.value = data.followed
  } catch {
    toast.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchStatus)
</script>
