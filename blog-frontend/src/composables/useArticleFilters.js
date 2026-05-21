import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export function useArticleFilters() {
  const route = useRoute()
  const router = useRouter()

  const selectedTags = ref(parseTagsFromQuery())
  const sortBy = ref(parseSortFromQuery())
  const pageNum = ref(Number(route.query.page) || 1)

  function parseTagsFromQuery() {
    const raw = route.query.tags
    if (!raw) return []
    return String(raw).split(',').map(t => t.trim()).filter(Boolean)
  }

  function parseSortFromQuery() {
    const raw = route.query.sort
    return raw === 'hottest' ? 'hottest' : 'newest'
  }

  function toggleTag(tag) {
    const idx = selectedTags.value.indexOf(tag)
    if (idx === -1) {
      selectedTags.value = [...selectedTags.value, tag]
    } else {
      selectedTags.value = selectedTags.value.filter(t => t !== tag)
    }
    pageNum.value = 1
    syncURL()
  }

  function setSort(sort) {
    sortBy.value = sort
    pageNum.value = 1
    syncURL()
  }

  function setPage(page) {
    pageNum.value = page
    syncURL()
  }

  function clearFilters() {
    selectedTags.value = []
    sortBy.value = 'newest'
    pageNum.value = 1
    syncURL()
  }

  function syncURL() {
    const query = {}
    if (route.query.q) query.q = route.query.q
    if (selectedTags.value.length > 0) query.tags = selectedTags.value.join(',')
    if (sortBy.value === 'hottest') query.sort = 'hottest'
    if (pageNum.value > 1) query.page = String(pageNum.value)
    router.replace({ query })
  }

  // Read URL → state (back/forward navigation)
  watch(() => route.query.tags, () => {
    const parsed = parseTagsFromQuery()
    if (parsed.join(',') !== selectedTags.value.join(',')) {
      selectedTags.value = parsed
    }
  })

  watch(() => route.query.sort, () => {
    sortBy.value = parseSortFromQuery()
  })

  watch(() => route.query.page, () => {
    pageNum.value = Number(route.query.page) || 1
  })

  function filterAndSort(articles) {
    let result = [...articles]

    if (selectedTags.value.length > 0) {
      result = result.filter(a => {
        const articleTags = a.tags ? a.tags.split(',').map(t => t.trim()) : []
        return selectedTags.value.some(st => articleTags.includes(st))
      })
    }

    if (sortBy.value === 'hottest') {
      result.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
    } else {
      result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }

    return result
  }

  return {
    selectedTags,
    sortBy,
    pageNum,
    toggleTag,
    setSort,
    setPage,
    clearFilters,
    filterAndSort,
  }
}
