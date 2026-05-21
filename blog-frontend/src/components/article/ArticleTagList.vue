<template>
  <div class="flex gap-1.5 flex-wrap">
    <router-link
      v-for="tag in tagList"
      :key="tag"
      :to="`/tag/${tag}`"
      class="inline-flex items-center px-2.5 py-1 text-xs font-medium text-primary bg-primary-light/50 rounded-full hover:bg-primary-light/70 transition-colors"
    >
      {{ tag }}
    </router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ tags: { type: String, default: '' } })

const tagList = computed(() => {
  if (!props.tags) return []
  return props.tags.split(',').map(t => t.trim()).filter(Boolean)
})
</script>
