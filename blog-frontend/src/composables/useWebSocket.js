import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { getToken } from '@/utils/storage'

export function useWebSocket(onMessage) {
  const connected = ref(false)
  let client = null

  function connect() {
    const token = getToken()
    if (!token) return

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

    client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        connected.value = true
        client.subscribe('/user/queue/notifications', (message) => {
          try {
            const data = JSON.parse(message.body)
            onMessage?.(data)
          } catch { /* skip */ }
        })
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: () => {
        connected.value = false
      },
      reconnectDelay: 5000
    })

    client.activate()
  }

  function disconnect() {
    if (client) {
      client.deactivate()
      client = null
      connected.value = false
    }
  }

  return { connected, connect, disconnect }
}
