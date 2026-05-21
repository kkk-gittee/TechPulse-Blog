import { ref } from 'vue'
import { getToken } from '@/utils/storage'

/**
 * SSE streaming hook for AI writing assistance.
 * Sends a POST request with the prompt and reads the SSE stream.
 *
 * Usage:
 *   const { stream, streaming, error, fetchAI } = useSSEWriter()
 *   await fetchAI('/api/ai/write', { prompt: '...' })
 */
export function useSSEWriter() {
  const stream = ref('')
  const streaming = ref(false)
  const error = ref(null)

  const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

  /**
   * Build a system-level writing prompt tailored for technical articles.
   * The frontend sends this as part of the request body so the AI receives
   * full context about the article being written.
   *
   * @param {'optimize'|'complete'|'generate'} mode
   * @param {object} context - { title, currentContent, selectedText }
   */
  function buildPrompt(mode, context = {}) {
    const { title = '', currentContent = '', selectedText = '' } = context

    const systemContext = `你是一位资深技术编辑，为技术博客"技术脉动"撰写内容。
写作风格要求：
- 语言专业但不晦涩，面向中高级开发者
- 适当使用比喻和类比帮助理解
- 代码示例优先使用 TypeScript / JavaScript
- 结构层次分明（H2 + H3），段落精简
- 避免营销用语，聚焦技术本质`

    const prompts = {
      optimize: `${systemContext}

当前文章标题：《${title}》

以下是我从文章中选中的一段文字，请帮我优化表达：

原始文本：
"""
${selectedText}
"""

要求：
1. 保持原意不变，增强技术表达的精确性
2. 改善句子节奏，长短句交替
3. 如果原文有口语化表达，转为更专业的书面语
4. 只返回优化后的文本，不要加任何解释或额外标记

优化后的文本：`,

      complete: `${systemContext}

当前文章标题：《${title}》

以下是文章当前内容（末尾三段）：

"""
${currentContent.slice(-800)}
"""

请根据以上内容，自然地续写下一段（约150-300字）：
1. 衔接上文逻辑，避免突兀转折
2. 如果是技术文章，补充一个相关的实践建议或深入思考
3. 保持与上文一致的语气和风格
4. 只返回续写内容，不要加标题或标记

续写：`,

      generate: `${systemContext}

请根据以下标题，生成一篇技术文章大纲：

文章标题：《${title}》

要求：
1. 包含引言、3-4个核心章节、总结
2. 每个章节简述要讨论的内容要点
3. 标注需要插入代码示例的位置（用 [代码示例：xxx] 表示）
4. 总长度约500-800字

文章大纲：`
    }

    return prompts[mode] || prompts.optimize
  }

  /**
   * Start an SSE streaming request to the AI writing endpoint.
   *
   * @param {'optimize'|'complete'|'generate'} mode
   * @param {object} context - article context
   * @returns {Promise<void>}
   */
  async function fetchAI(mode, context = {}) {
    stream.value = ''
    streaming.value = true
    error.value = null

    const token = getToken()
    const prompt = buildPrompt(mode, context)

    try {
      const response = await fetch(`${API_BASE}/api/ai/write`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: JSON.stringify({
          prompt,
          mode,
          title: context.title || '',
          sessionId: context.sessionId || null
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

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
              if (eventType === 'message' || !eventType) {
                stream.value += data.content || ''
              } else if (eventType === 'complete') {
                stream.value = data.content || stream.value
              } else if (eventType === 'error') {
                error.value = data.error || 'AI 服务异常'
              }
            } catch { /* skip malformed data */ }
          }
        }
      }
    } catch (err) {
      error.value = err.message || '连接失败'
    } finally {
      streaming.value = false
    }
  }

  function reset() {
    stream.value = ''
    streaming.value = false
    error.value = null
  }

  return { stream, streaming, error, fetchAI, buildPrompt, reset }
}
