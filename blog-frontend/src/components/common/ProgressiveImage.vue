<template>
  <div class="relative overflow-hidden" :style="{ backgroundColor: placeholderColor, aspectRatio }">
    <img
      :src="src"
      :alt="alt"
      :loading="lazy ? 'lazy' : undefined"
      class="w-full h-full object-cover transition-all duration-500 ease-out"
      :class="loaded ? 'blur-0 scale-100' : 'blur-xl scale-105'"
      @load="loaded = true"
      @error="handleError"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  src: { type: String, required: true },
  alt: { type: String, default: '' },
  lazy: { type: Boolean, default: true },
  aspectRatio: { type: String, default: '16/10' },
})

const emit = defineEmits(['error'])
const loaded = ref(false)

const placeholderColor = computed(() => {
  let hash = 0
  for (let i = 0; i < (props.src || '').length; i++) {
    hash = ((hash << 5) - hash + props.src.charCodeAt(i)) | 0
  }
  const hue = [210, 220, 230, 215, 225][Math.abs(hash) % 5]
  return `hsl(${hue}, 30%, 92%)`
})

function handleError(e) {
  loaded.value = true
  emit('error', e)
}
</script>
