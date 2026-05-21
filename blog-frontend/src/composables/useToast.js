import { ref } from 'vue'

const toasts = ref([])
let toastId = 0

function addToast(message, type = 'info', duration = 3000) {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, duration)
}

export const toast = {
  success(msg) { addToast(msg, 'success') },
  error(msg) { addToast(msg, 'error', 4000) },
  warning(msg) { addToast(msg, 'warning', 3500) },
  info(msg) { addToast(msg, 'info') }
}

export { toasts }
