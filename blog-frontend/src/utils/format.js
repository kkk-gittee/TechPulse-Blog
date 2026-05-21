export function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function formatCount(count) {
  if (count === undefined || count === null) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return String(count)
}

export function estimateReadingTime(markdown) {
  if (!markdown) return 1
  const withoutCode = markdown.replace(/```[\s\S]*?```/g, '')
  const withoutInlineCode = withoutCode.replace(/`[^`]*`/g, '')
  const chineseChars = (withoutInlineCode.match(/[一-鿿㐀-䶿]/g) || []).length
  const englishWords = withoutInlineCode
    .replace(/[^\x00-\x7F]+/g, ' ')
    .split(/\s+/)
    .filter(w => w.length > 0).length

  let minutes = (chineseChars / 200) + (englishWords / 238)
  const codeBlocks = markdown.match(/```[\s\S]*?```/g) || []
  codeBlocks.forEach(block => {
    const lines = block.split('\n').length
    minutes += lines * 0.02
  })
  return Math.max(1, Math.round(minutes))
}
