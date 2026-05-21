import { onMounted, onUnmounted } from 'vue'

export function useKeyboard(shortcuts) {
  function onKeydown(e) {
    const target = e.target
    const isInput = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable

    for (const s of shortcuts) {
      const keyMatch = e.key.toLowerCase() === s.key.toLowerCase()
      const ctrlMatch = s.ctrl ? (e.ctrlKey || e.metaKey) : !e.ctrlKey && !e.metaKey

      if (keyMatch && ctrlMatch) {
        if (isInput && !s.allowInInput) continue
        e.preventDefault()
        s.handler(e)
        return
      }
    }
  }

  onMounted(() => window.addEventListener('keydown', onKeydown))
  onUnmounted(() => window.removeEventListener('keydown', onKeydown))
}
