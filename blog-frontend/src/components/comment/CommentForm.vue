<template>
  <div class="mb-5">
    <div v-if="replyTo" class="flex items-center gap-2 mb-2 px-1">
      <span class="text-xs text-on-surface-variant">回复</span>
      <span class="text-xs font-medium text-primary">@{{ replyTo.nickname || replyTo.username }}</span>
    </div>
    <textarea
      ref="textareaRef"
      v-model="content"
      class="w-full bg-surface-subtle/50 border border-outline/10 rounded-2xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all resize-none"
      placeholder="写下你的评论..."
      rows="3"
      maxlength="500"
    />
    <div class="flex justify-end gap-2 mt-2.5">
      <button
        v-if="replyTo"
        class="px-3.5 py-1.5 text-xs font-medium text-on-surface-variant border border-outline/20 rounded-lg hover:text-on-surface hover:border-outline/30 transition-all"
        @click="$emit('cancel')"
      >
        取消
      </button>
      <button
        class="px-4 py-1.5 text-xs font-semibold text-white bg-primary rounded-lg hover:opacity-90 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        :disabled="!content.trim() || loading"
        @click="handleSubmit"
      >
        {{ loading ? '发送中...' : (replyTo ? '回复' : '发表评论') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  replyTo: { type: Object, default: null }
})

const emit = defineEmits(['submit', 'cancel'])
const content = ref('')
const loading = ref(false)
const textareaRef = ref(null)

watch(() => props.replyTo, async (val) => {
  if (val) {
    await nextTick()
    textareaRef.value?.focus()
  }
})

async function handleSubmit() {
  if (!content.value.trim()) return
  loading.value = true
  try {
    emit('submit', {
      content: content.value.trim(),
      parentId: props.replyTo?.parentId || props.replyTo?.id || null,
      replyUserId: props.replyTo?.userId || null
    })
    content.value = ''
  } finally {
    loading.value = false
  }
}
</script>
