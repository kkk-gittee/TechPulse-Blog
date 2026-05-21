import { ref, nextTick, watch } from 'vue'

export function useToc() {
  const headings = ref([])
  const activeId = ref('')
  let observer = null

  function extractHeadings() {
    nextTick(() => {
      const elements = document.querySelectorAll('.markdown-body h2, .markdown-body h3')
      headings.value = Array.from(elements).map((el, index) => {
        const id = el.id || `heading-${index}`
        if (!el.id) el.id = id
        return { id, text: el.textContent, level: Number(el.tagName[1]) }
      })
      setupObserver()
    })
  }

  function setupObserver() {
    observer?.disconnect()
    const elements = headings.value.map(h => document.getElementById(h.id)).filter(Boolean)
    observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter(e => e.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
        if (visible.length > 0) activeId.value = visible[0].target.id
      },
      { rootMargin: '-64px 0px -60% 0px', threshold: 0 }
    )
    elements.forEach(el => observer.observe(el))
  }

  function scrollToHeading(id) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return { headings, activeId, extractHeadings, scrollToHeading }
}
