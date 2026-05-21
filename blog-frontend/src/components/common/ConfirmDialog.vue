<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="visible"
        class="fixed inset-0 z-[300] flex items-center justify-center p-6"
        @click.self="onCancel"
      >
        <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" />
        <div class="relative bg-surface-card rounded-2xl shadow-2xl border border-outline/10 p-8 max-w-sm w-full">
          <div class="flex items-start gap-4">
            <div
              class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
              :class="type === 'danger' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' : 'bg-primary-light text-primary'"
            >
              <AlertTriangle v-if="type === 'danger'" :size="20" />
              <Info v-else :size="20" />
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="font-display font-bold text-lg text-on-surface mb-1">{{ title }}</h3>
              <p class="text-sm text-on-surface-variant leading-relaxed">{{ message }}</p>
            </div>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button
              class="px-4 py-2 text-sm font-medium text-on-surface-variant hover:text-on-surface hover:bg-surface-hover rounded-xl transition-all"
              @click="onCancel"
            >
              {{ cancelText }}
            </button>
            <button
              class="px-5 py-2 text-sm font-semibold text-white rounded-xl transition-all active:scale-95"
              :class="type === 'danger' ? 'bg-red-600 hover:bg-red-700' : 'bg-slate-900 hover:bg-black dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200'"
              @click="onConfirm"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { Info, AlertTriangle } from 'lucide-vue-next'

defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' },
  type: { type: String, default: 'info' }
})

const emit = defineEmits(['confirm', 'cancel'])

function onConfirm() {
  emit('confirm')
}

function onCancel() {
  emit('cancel')
}
</script>

<style scoped>
.modal-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.modal-leave-active {
  transition: all 0.2s ease-in;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from > div:last-child,
.modal-leave-to > div:last-child {
  transform: scale(0.95) translateY(8px);
  opacity: 0;
}
</style>
