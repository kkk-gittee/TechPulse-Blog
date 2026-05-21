import request from './request'
import { getToken } from '@/utils/storage'

export function chat(message, sessionId, { onStart, onMessage, onComplete, onError }) {
  const token = getToken()
  const url = `${import.meta.env.VITE_API_BASE_URL || ''}/api/ai/chat`

  const body = { message }
  if (sessionId) body.sessionId = sessionId

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(body)
  }).then(async (response) => {
    if (!response.ok) {
      onError?.(new Error(`HTTP ${response.status}`))
      return
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let completed = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let eventType = ''
      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventType = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const dataStr = line.slice(5).trim()
          try {
            const data = JSON.parse(dataStr)
            if (eventType === 'start') {
              onStart?.(data)
            } else if (eventType === 'message') {
              onMessage?.(data)
            } else if (eventType === 'complete') {
              completed = true
              onComplete?.(data)
            } else if (eventType === 'error') {
              onError?.(new Error(data.error))
            }
          } catch { /* skip malformed data */ }
        }
      }
    }
    // Stream ended — fire onComplete if not already called
    if (!completed) {
      setTimeout(() => onComplete?.({}), 0)
    }
  }).catch((err) => {
    onError?.(err)
  })
}

export function getChatHistory(sessionId) {
  return request.get(`/api/ai/history/${sessionId}`)
}

export function clearChatHistory(sessionId) {
  return request.delete(`/api/ai/history/${sessionId}`)
}
