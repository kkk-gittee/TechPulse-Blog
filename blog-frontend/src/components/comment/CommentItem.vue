<template>
  <div class="py-5 border-b border-outline/10 last:border-b-0">
    <div class="flex items-center gap-3 mb-2.5">
      <div class="w-9 h-9 rounded-full bg-surface-subtle flex items-center justify-center text-sm font-semibold text-on-surface-variant flex-shrink-0">
        {{ comment.nickname?.charAt(0) || 'U' }}
      </div>
      <div class="flex flex-col">
        <span class="font-semibold text-sm text-on-surface">{{ comment.nickname || comment.username }}</span>
        <span class="text-xs text-on-surface-variant">{{ formatDate(comment.createTime) }}</span>
      </div>
    </div>

    <div class="pl-12">
      <p v-if="comment.replyNickname" class="text-xs text-on-surface-variant mb-1">
        回复 <span class="text-primary font-medium">@{{ comment.replyNickname }}</span>
      </p>
      <p class="text-sm text-on-surface leading-relaxed">{{ comment.content }}</p>

      <div class="flex items-center gap-2 mt-2">
        <button
          class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-md transition-all"
          :class="liked ? 'text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-subtle'"
          @click="handleLike"
        >
          <Heart :size="13" :class="liked ? 'fill-current' : ''" />
          <span v-if="likeCount > 0">{{ likeCount }}</span>
        </button>
        <button
          class="inline-flex items-center px-2.5 py-1 text-xs font-medium text-on-surface-variant hover:text-on-surface hover:bg-surface-subtle rounded-md transition-all"
          @click="$emit('reply', comment)"
        >
          回复
        </button>
        <button
          v-if="currentUserId === comment.userId"
          class="inline-flex items-center px-2.5 py-1 text-xs font-medium text-on-surface-variant hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md transition-all"
          @click="$emit('delete', comment.id)"
        >
          删除
        </button>
      </div>

      <!-- Nested Replies -->
      <div v-if="comment.replies && comment.replies.length > 0" class="mt-3 pl-4 border-l-2 border-outline/15">
        <CommentItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :comment="reply"
          :current-user-id="currentUserId"
          @reply="(c) => $emit('reply', c)"
          @delete="(id) => $emit('delete', id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Heart } from 'lucide-vue-next'
import { toggleCommentLike } from '@/api/comment'
import { formatDate } from '@/utils/format'

const props = defineProps({
  comment: { type: Object, required: true },
  currentUserId: { type: [Number, String], default: null }
})

defineEmits(['reply', 'delete'])

const liked = ref(!!props.comment.isLiked)
const likeCount = ref(props.comment.likeCount || 0)

async function handleLike() {
  const prevLiked = liked.value
  const prevCount = likeCount.value

  // Optimistic update
  liked.value = !prevLiked
  likeCount.value += liked.value ? 1 : -1

  try {
    const result = await toggleCommentLike(props.comment.id)
    // Sync with server state
    liked.value = result.liked
    likeCount.value = prevCount + (result.liked ? 1 : -1)
  } catch {
    // Rollback on API failure
    liked.value = prevLiked
    likeCount.value = prevCount
  }
}
</script>
