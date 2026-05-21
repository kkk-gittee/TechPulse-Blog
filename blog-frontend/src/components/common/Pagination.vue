<template>
  <div v-if="total > pageSize" class="flex items-center justify-center gap-4 mt-10 py-4">
    <button
      class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
      :disabled="currentPage <= 1"
      @click="go(currentPage - 1)"
    >
      <ChevronLeft :size="16" />
      上一页
    </button>
    <span class="text-sm text-on-surface-variant tabular-nums">{{ currentPage }} / {{ totalPages }}</span>
    <button
      class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
      :disabled="currentPage >= totalPages"
      @click="go(currentPage + 1)"
    >
      下一页
      <ChevronRight :size="16" />
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  total: { type: Number, default: 0 },
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

const emit = defineEmits(['update:pageNum', 'page-change'])

const currentPage = ref(props.pageNum)

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

watch(() => props.pageNum, (val) => { currentPage.value = val })

function go(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  emit('update:pageNum', page)
  emit('page-change', { pageNum: page, pageSize: props.pageSize })
}
</script>
