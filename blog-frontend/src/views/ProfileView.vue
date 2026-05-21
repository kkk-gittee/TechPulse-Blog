<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 md:px-8 pb-20">
    <!-- Loading Skeleton -->
    <template v-if="loadingUser">
      <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-8 mb-6 animate-pulse">
        <div class="flex items-center gap-6">
          <div class="w-20 h-20 rounded-full bg-surface-subtle" />
          <div class="flex-1 space-y-3">
            <div class="h-6 w-40 bg-surface-subtle rounded-lg" />
            <div class="h-4 w-64 bg-surface-subtle rounded-lg" />
            <div class="flex gap-7">
              <div class="h-5 w-16 bg-surface-subtle rounded" />
              <div class="h-5 w-16 bg-surface-subtle rounded" />
              <div class="h-5 w-16 bg-surface-subtle rounded" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Error State -->
    <div v-else-if="loadError" class="text-center py-20">
      <div class="w-16 h-16 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4">
        <AlertCircle :size="32" class="text-red-400" />
      </div>
      <h2 class="font-display font-bold text-xl text-on-surface mb-2">加载失败</h2>
      <p class="text-sm text-on-surface-variant mb-6">{{ loadError }}</p>
      <button
        class="px-6 py-2.5 bg-primary text-white rounded-xl font-medium text-sm hover:opacity-90 transition-opacity"
        @click="fetchUser(); fetchArticles()"
      >
        重新加载
      </button>
    </div>

    <!-- Profile Content -->
    <template v-else-if="userInfo">
      <!-- Profile Card -->
      <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-8 mb-6">
        <div class="flex items-center gap-6">
          <div class="w-20 h-20 rounded-full bg-primary-light/50 text-primary flex items-center justify-center text-3xl font-display font-bold flex-shrink-0 border-4 border-surface-card shadow-md">
            {{ userInfo.nickname?.charAt(0) || 'U' }}
          </div>
          <div class="flex-1 min-w-0">
            <h2 class="font-display font-bold text-2xl text-on-surface tracking-tight">
              {{ userInfo.nickname || userInfo.username }}
            </h2>
            <p class="text-on-surface-variant text-sm mt-1">{{ userInfo.bio || '这个人很懒，什么都没写' }}</p>
            <div class="flex items-center gap-7 mt-4">
              <div class="flex items-baseline gap-1.5">
                <span class="text-lg font-bold text-primary">{{ userInfo.articleCount || 0 }}</span>
                <span class="text-xs text-on-surface-variant">文章</span>
              </div>
              <div class="flex items-baseline gap-1.5">
                <span class="text-lg font-bold text-primary">{{ userInfo.followerCount || 0 }}</span>
                <span class="text-xs text-on-surface-variant">粉丝</span>
              </div>
              <div class="flex items-baseline gap-1.5">
                <span class="text-lg font-bold text-primary">{{ userInfo.followingCount || 0 }}</span>
                <span class="text-xs text-on-surface-variant">关注</span>
              </div>
            </div>
          </div>
          <div v-if="isOtherUser" class="flex-shrink-0">
            <FollowButton :user-id="userId" />
          </div>
          <button
            v-else
            class="flex-shrink-0 flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-light/40 text-primary font-medium text-sm hover:bg-primary-light/60 transition-colors"
            @click="openEditModal"
          >
            <Settings2 :size="16" />
            编辑资料
          </button>
        </div>
      </div>

      <!-- Articles Section -->
      <div class="bg-surface-card rounded-2xl bento-shadow border border-outline/15 p-8">
        <h3 class="font-display font-bold text-lg text-on-surface mb-6">TA 的文章</h3>
        <div class="flex flex-col gap-4">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
        <div v-if="!loading && articles.length === 0" class="text-center py-12 text-sm text-on-surface-variant">
          暂无文章
        </div>

        <!-- Pagination -->
        <div v-if="total > 10" class="flex items-center justify-center gap-4 mt-8">
          <button
            class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
            :disabled="pageNum <= 1"
            @click="goPage(pageNum - 1)"
          >
            上一页
          </button>
          <span class="text-sm text-on-surface-variant tabular-nums">{{ pageNum }} / {{ Math.ceil(total / 10) }}</span>
          <button
            class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium text-on-surface-variant bg-surface-card border border-outline/20 rounded-lg hover:bg-surface-subtle hover:border-outline/30 transition-all disabled:opacity-35 disabled:cursor-not-allowed"
            :disabled="pageNum >= Math.ceil(total / 10)"
            @click="goPage(pageNum + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </template>

    <!-- Edit Profile Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showEditModal" class="fixed inset-0 z-[200] flex items-center justify-center p-6">
          <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="cancelEdit" />
          <div class="relative bg-surface-card rounded-3xl shadow-2xl p-8 max-w-lg w-full max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between mb-6">
              <h3 class="font-display font-bold text-xl text-on-surface">编辑资料</h3>
              <button
                class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-surface-subtle transition-colors"
                @click="cancelEdit"
              >
                <X :size="18" class="text-on-surface-variant" />
              </button>
            </div>

            <form class="space-y-5" @submit.prevent="saveProfile">
              <!-- Avatar -->
              <div class="space-y-2">
                <label class="font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest" for="avatar">
                  头像 URL
                </label>
                <div class="flex items-center gap-4">
                  <div class="w-14 h-14 rounded-full bg-primary-light/50 text-primary flex items-center justify-center text-xl font-display font-bold flex-shrink-0 border-2 border-white shadow-sm">
                    {{ editForm.nickname?.charAt(0) || 'U' }}
                  </div>
                  <input
                    id="avatar"
                    v-model="editForm.avatar"
                    class="flex-1 bg-surface-subtle border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all"
                    placeholder="https://..."
                  />
                </div>
              </div>

              <!-- Nickname -->
              <div class="space-y-2">
                <label class="font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest" for="nickname">
                  昵称
                </label>
                <input
                  id="nickname"
                  v-model="editForm.nickname"
                  class="w-full bg-surface-subtle border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all"
                  placeholder="输入你的昵称"
                  maxlength="30"
                />
              </div>

              <!-- Bio -->
              <div class="space-y-2">
                <label class="font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest" for="bio">
                  个人简介
                </label>
                <textarea
                  id="bio"
                  v-model="editForm.bio"
                  class="w-full bg-surface-subtle border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all resize-none"
                  placeholder="介绍一下你自己..."
                  rows="3"
                  maxlength="200"
                />
                <div class="text-right text-xs text-on-surface-variant/40 tabular-nums">{{ editForm.bio?.length || 0 }}/200</div>
              </div>

              <!-- Email -->
              <div class="space-y-2">
                <label class="font-display font-bold text-[10px] text-on-surface-variant/60 uppercase tracking-widest" for="email">
                  邮箱
                </label>
                <input
                  id="email"
                  v-model="editForm.email"
                  type="email"
                  class="w-full bg-surface-subtle border border-outline/20 rounded-xl py-3 px-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant/40 focus:ring-4 focus:ring-primary/5 focus:border-primary/20 outline-none transition-all"
                  placeholder="your@email.com"
                />
              </div>

              <!-- Buttons -->
              <div class="flex gap-3 pt-2">
                <button
                  type="button"
                  class="flex-1 py-3 rounded-xl bg-surface-subtle text-on-surface-variant font-medium text-sm hover:bg-surface-card transition-colors"
                  @click="cancelEdit"
                >
                  取消
                </button>
                <button
                  type="submit"
                  class="flex-1 py-3 rounded-xl bg-primary text-white font-bold text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="saving"
                >
                  <span v-if="saving" class="inline-flex items-center gap-2">
                    <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    保存中
                  </span>
                  <span v-else>保存</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicUser } from '@/api/auth'
import { getUserArticles } from '@/api/article'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/article/ArticleCard.vue'
import FollowButton from '@/components/user/FollowButton.vue'
import { Settings2, AlertCircle, X } from 'lucide-vue-next'
import { toast } from '@/composables/useToast'

const route = useRoute()
const userStore = useUserStore()
const userInfo = ref(null)
const articles = ref([])
const loading = ref(false)
const loadingUser = ref(true)
const loadError = ref('')
const pageNum = ref(1)
const total = ref(0)

// Edit modal state
const showEditModal = ref(false)
const saving = ref(false)
const editForm = reactive({ nickname: '', bio: '', avatar: '', email: '' })

const userId = computed(() => route.params.id)
const isOtherUser = computed(() => userStore.userInfo?.id !== Number(userId.value))

async function fetchUser() {
  loadingUser.value = true
  loadError.value = ''
  try {
    userInfo.value = await getPublicUser(userId.value)
  } catch (e) {
    loadError.value = e?.message || '加载用户信息失败'
    userInfo.value = null
  } finally {
    loadingUser.value = false
  }
}

async function fetchArticles() {
  loading.value = true
  try {
    const data = await getUserArticles(userId.value, { pageNum: pageNum.value, pageSize: 10 })
    articles.value = data.records || []
    total.value = data.total || 0
  } catch {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goPage(page) {
  pageNum.value = page
  fetchArticles()
}

// ─── Edit Profile ────────────────────────────────────────

function openEditModal() {
  editForm.nickname = userInfo.value?.nickname || ''
  editForm.bio = userInfo.value?.bio || ''
  editForm.avatar = userInfo.value?.avatar || ''
  editForm.email = userInfo.value?.email || ''
  showEditModal.value = true
}

function cancelEdit() {
  showEditModal.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    await userStore.updateProfile({
      nickname: editForm.nickname,
      bio: editForm.bio,
      avatar: editForm.avatar,
      email: editForm.email
    })
    // Refresh local display
    if (userInfo.value) {
      userInfo.value.nickname = editForm.nickname
      userInfo.value.bio = editForm.bio
      userInfo.value.avatar = editForm.avatar
      userInfo.value.email = editForm.email
    }
    showEditModal.value = false
    toast.success('资料已更新')
  } catch (e) {
    toast.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

watch(() => route.params.id, () => {
  pageNum.value = 1
  fetchUser()
  fetchArticles()
})

onMounted(() => {
  fetchUser()
  fetchArticles()
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
.modal-enter-active > div:last-child,
.modal-leave-active > div:last-child {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
