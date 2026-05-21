<template>
  <Transition name="back-to-top">
    <button
      v-if="visible"
      class="fixed bottom-8 right-8 z-50 w-11 h-11 flex items-center justify-center bg-surface-card border border-outline/20 rounded-2xl shadow-lg hover:shadow-xl hover:-translate-y-0.5 active:scale-95 transition-all duration-300 group"
      @click="scrollToTop"
    >
      <ArrowUp :size="20" class="text-on-surface-variant group-hover:text-primary transition-colors" />
    </button>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowUp } from 'lucide-vue-next'

const visible = ref(false)

function onScroll() {
  visible.value = window.scrollY > 300
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.back-to-top-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.back-to-top-leave-active {
  transition: all 0.2s ease-in;
}
.back-to-top-enter-from,
.back-to-top-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.9);
}
</style>
