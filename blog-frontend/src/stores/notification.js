import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from './user'
import { toast } from '@/composables/useToast'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)

  function handleMessage(data) {
    notifications.value.unshift(data)
    unreadCount.value++

    const title = data.type === 'LIKE' ? '点赞通知' :
                  data.type === 'COMMENT' ? '评论通知' :
                  data.type === 'FOLLOW' ? '关注通知' : '通知'
    toast.info(`${title} - ${data.fromNickname || data.fromUsername} ${data.content}`)
  }

  const { connected, connect, disconnect } = useWebSocket(handleMessage)

  function markAllRead() {
    unreadCount.value = 0
  }

  function init() {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      connect()
    }
  }

  return { notifications, unreadCount, connected, connect, disconnect, markAllRead, init }
})
