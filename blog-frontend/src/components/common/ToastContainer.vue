<template>
  <div class="fixed top-20 right-6 z-[200] flex flex-col gap-3 pointer-events-none">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        :class="[
          'pointer-events-auto px-5 py-3 rounded-2xl text-sm font-medium shadow-lg backdrop-blur-xl border transition-all',
          t.type === 'success' && 'bg-green-50/90 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-400 dark:border-green-800/30',
          t.type === 'error' && 'bg-red-50/90 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800/30',
          t.type === 'warning' && 'bg-amber-50/90 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800/30',
          t.type === 'info' && 'bg-surface-card/90 text-on-surface border-outline/20'
        ]"
      >
        {{ t.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { toasts } from '@/composables/useToast'
</script>

<style scoped>
.toast-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(40px) scale(0.95);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(40px) scale(0.95);
}
.toast-move {
  transition: transform 0.3s ease;
}
</style>
