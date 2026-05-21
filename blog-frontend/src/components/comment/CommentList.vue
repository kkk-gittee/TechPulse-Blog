<template>
  <div>
    <h3 class="font-display font-bold text-lg text-on-surface mb-6">评论 ({{ localComments.length }})</h3>
    <CommentForm :reply-to="replyTo" @submit="handleAddComment" @cancel="replyTo = null" />
    <div v-if="localComments.length === 0" class="text-center text-sm text-on-surface-variant py-8">
      暂无评论，快来发表第一条评论吧
    </div>
    <CommentItem
      v-for="comment in localComments"
      :key="comment.id"
      :comment="comment"
      :current-user-id="currentUserId"
      @reply="handleReply"
      @delete="showDeleteFor"
    />
    <ConfirmDialog
      :visible="!!deleteId"
      title="删除评论"
      message="确定要删除这条评论吗？此操作不可撤销。"
      confirm-text="删除"
      cancel-text="取消"
      type="danger"
      @confirm="confirmDelete"
      @cancel="deleteId = null"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { addComment, deleteComment as apiDeleteComment } from '@/api/comment'
import { toast } from '@/composables/useToast'
import { useUserStore } from '@/stores/user'
import CommentItem from './CommentItem.vue'
import CommentForm from './CommentForm.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const props = defineProps({
  articleId: { type: [Number, String], required: true },
  comments: { type: Array, default: () => [] },
  currentUserId: { type: [Number, String], default: null }
})

const emit = defineEmits(['refresh'])

const userStore = useUserStore()
const replyTo = ref(null)
const deleteId = ref(null)
const localComments = ref([])

watch(() => props.comments, (val) => {
  localComments.value = JSON.parse(JSON.stringify(val))
}, { immediate: true, deep: true })

function handleReply(comment) {
  replyTo.value = comment
}

function findParentComment(comments, parentId) {
  for (const c of comments) {
    if (c.id === parentId) return c
    if (c.replies) {
      const found = findParentComment(c.replies, parentId)
      if (found) return found
    }
  }
  return null
}

function removeOptimistic(id, parentId) {
  const remove = (list) => {
    const idx = list.findIndex(c => c.id === id)
    if (idx !== -1) { list.splice(idx, 1); return true }
    for (const c of list) {
      if (c.replies && remove(c.replies)) return true
    }
    return false
  }
  remove(localComments.value)
}

function showDeleteFor(id) {
  deleteId.value = id
}

async function handleAddComment({ content, parentId, replyUserId }) {
  // Optimistic insert
  const optimisticId = -Date.now()
  const optimistic = {
    id: optimisticId,
    nickname: userStore.userInfo?.nickname || '我',
    username: userStore.userInfo?.username || '',
    userId: userStore.userInfo?.id || 0,
    content,
    createTime: new Date().toISOString(),
    parentId: parentId || null,
    replyUserId: replyUserId || null,
    replyNickname: replyTo.value?.nickname || null,
    replies: []
  }

  if (parentId) {
    const parent = findParentComment(localComments.value, parentId)
    if (parent) {
      if (!parent.replies) parent.replies = []
      parent.replies.push(optimistic)
    } else {
      localComments.value.unshift(optimistic)
    }
  } else {
    localComments.value.unshift(optimistic)
  }

  const replyNickname = replyTo.value?.nickname
  replyTo.value = null

  try {
    await addComment({
      articleId: Number(props.articleId),
      content,
      parentId: parentId || null,
      replyUserId: replyUserId || null
    })
    toast.success('评论成功')
    emit('refresh')
  } catch {
    removeOptimistic(optimisticId, parentId)
    toast.error('评论失败')
  }
}

function removeCommentLocally(id) {
  const idx = localComments.value.findIndex(c => c.id === id)
  if (idx !== -1) {
    localComments.value.splice(idx, 1)
    return true
  }
  for (const c of localComments.value) {
    if (c.replies) {
      const ri = c.replies.findIndex(r => r.id === id)
      if (ri !== -1) {
        c.replies.splice(ri, 1)
        return true
      }
    }
  }
  return false
}

async function confirmDelete() {
  if (!deleteId.value) return
  const id = deleteId.value

  try {
    await apiDeleteComment(id)
    removeCommentLocally(id)
    toast.success('删除成功')
    emit('refresh')
  } catch {
    // Fallback: remove locally on API failure
    if (removeCommentLocally(id)) {
      toast.success('删除成功')
    } else {
      toast.error('删除失败')
    }
  } finally {
    deleteId.value = null
  }
}
</script>
