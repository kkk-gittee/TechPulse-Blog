<template>
  <nav
    class="fixed top-0 w-full z-50 transition-all duration-300"
    :class="scrolled
      ? 'bg-surface-card/80 backdrop-blur-xl border-b border-outline/15 shadow-[0_8px_32px_rgba(0,0,0,0.04)]'
      : 'bg-surface-card/30 backdrop-blur-md border-b border-transparent'"
  >
    <div class="flex justify-between items-center h-16 px-4 sm:px-6 md:px-8 max-w-7xl mx-auto">
      <!-- Brand Logo -->
      <router-link to="/" class="flex items-center gap-2 group shrink-0">
        <div class="w-8 h-8 rounded-lg bg-primary flex items-center justify-center text-white transition-transform group-hover:scale-105">
          <Brain :size="20" />
        </div>
        <span class="font-display font-bold text-lg sm:text-xl tracking-tighter text-on-surface">
          技术脉动 AI
        </span>
      </router-link>

      <!-- Navigation Links (Desktop) -->
      <div class="hidden md:flex items-center gap-2 font-display font-medium text-sm tracking-tight">
        <router-link
          to="/"
          class="px-4 py-2 rounded-lg transition-all"
          :class="route.path === '/' ? 'text-primary bg-surface-subtle' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-subtle'"
        >
          首页
        </router-link>
        <router-link
          v-if="userStore.isLoggedIn"
          to="/write"
          class="px-4 py-2 text-on-surface-variant hover:text-on-surface hover:bg-surface-subtle rounded-lg transition-all"
        >
          写文章
        </router-link>
        <router-link
          v-if="userStore.isLoggedIn"
          to="/ai-chat"
          class="px-4 py-2 rounded-lg transition-all"
          :class="route.path === '/ai-chat' ? 'text-primary bg-surface-subtle' : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-subtle'"
        >
          AI 助手
        </router-link>
      </div>

      <!-- Trailing Actions -->
      <div class="flex items-center gap-1 sm:gap-3">
        <!-- Search -->
        <div class="relative hidden sm:block">
          <input
            v-model="searchKeyword"
            class="w-32 md:w-48 h-9 pl-9 pr-3 rounded-full border border-outline/20 bg-surface-card text-sm font-sans text-on-surface placeholder:text-on-surface-variant outline-none transition-all focus:border-primary focus:w-40 md:focus:w-64"
            placeholder="搜索..."
            @keyup.enter="handleSearch"
          />
          <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none" />
        </div>

        <!-- Mobile Search Button -->
        <button
          class="sm:hidden p-2 text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all"
          @click="showMobileSearch = !showMobileSearch"
        >
          <Search :size="20" />
        </button>

        <!-- Theme Toggle -->
        <button
          class="p-2 text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all"
          @click="themeStore.toggle()"
          :title="themeStore.isDark ? '切换亮色模式' : '切换暗色模式'"
        >
          <Sun v-if="themeStore.isDark" :size="20" />
          <Moon v-else :size="20" />
        </button>

        <!-- Hamburger Menu Button (Mobile) -->
        <button
          class="md:hidden p-2 text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all"
          @click="showMobileMenu = !showMobileMenu"
        >
          <Menu v-if="!showMobileMenu" :size="20" />
          <X v-else :size="20" />
        </button>

        <template v-if="userStore.isLoggedIn">
          <!-- Notifications -->
          <button
            class="relative p-2 text-on-surface-variant hover:bg-surface-subtle rounded-lg transition-all"
            @click="router.push('/notifications')"
          >
            <Bell :size="20" />
            <span
              v-if="notificationStore.unreadCount > 0"
              class="absolute -top-0.5 -right-0.5 min-w-[16px] h-4 px-1 bg-primary text-white text-[10px] font-semibold rounded-full flex items-center justify-center"
            >
              {{ notificationStore.unreadCount }}
            </span>
          </button>

          <!-- User Avatar Dropdown -->
          <div class="relative hidden sm:block" ref="dropdownRef">
            <button
              class="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center text-sm font-semibold transition-shadow hover:shadow-[0_0_0_3px_rgba(0,47,167,0.15)]"
              @click="showDropdown = !showDropdown"
            >
              {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
            </button>
            <Transition name="dropdown">
              <div
                v-if="showDropdown"
                class="absolute right-0 top-full mt-2 w-44 bg-surface-card rounded-xl border border-outline/15 shadow-xl py-2 z-50"
              >
                <router-link
                  :to="`/user/${userStore.userInfo?.id}`"
                  class="block px-4 py-2.5 text-sm text-on-surface hover:bg-surface-subtle transition-colors"
                  @click="showDropdown = false"
                >
                  个人主页
                </router-link>
                <router-link
                  to="/favorites"
                  class="block px-4 py-2.5 text-sm text-on-surface hover:bg-surface-subtle transition-colors"
                  @click="showDropdown = false"
                >
                  我的收藏
                </router-link>
                <router-link
                  to="/history"
                  class="block px-4 py-2.5 text-sm text-on-surface hover:bg-surface-subtle transition-colors"
                  @click="showDropdown = false"
                >
                  浏览历史
                </router-link>
                <div class="my-1 border-t border-outline/15" />
                <button
                  class="w-full text-left px-4 py-2.5 text-sm text-on-surface hover:bg-surface-subtle transition-colors"
                  @click="handleLogout"
                >
                  退出登录
                </button>
              </div>
            </Transition>
          </div>
        </template>

        <template v-else>
          <router-link
            to="/login"
            class="px-4 py-2 text-sm font-medium text-on-surface-variant hover:text-on-surface rounded-lg transition-all"
          >
            登录
          </router-link>
          <router-link
            to="/register"
            class="bg-primary text-white px-5 py-2 rounded-full font-display font-medium text-sm hover:opacity-90 active:scale-95 transition-all shadow-md flex items-center gap-2"
          >
            注册
          </router-link>
        </template>
      </div>
    </div>

    <!-- Mobile Search Bar -->
    <Transition name="slide-down">
      <div v-if="showMobileSearch" class="px-4 pb-3 md:hidden">
        <div class="relative">
          <input
            v-model="searchKeyword"
            class="w-full h-10 pl-10 pr-4 rounded-xl border border-outline/20 bg-surface-card text-sm text-on-surface placeholder:text-on-surface-variant outline-none focus:border-primary"
            placeholder="搜索文章..."
            @keyup.enter="handleSearch"
          />
          <Search :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-on-surface-variant" />
        </div>
      </div>
    </Transition>

    <!-- Mobile Menu Overlay -->
    <Transition name="fade">
      <div
        v-if="showMobileMenu"
        class="fixed inset-0 bg-black/20 z-40 md:hidden"
        @click="showMobileMenu = false"
      />
    </Transition>

    <!-- Mobile Menu Drawer -->
    <Transition name="slide-down">
      <div
        v-if="showMobileMenu"
        class="absolute top-16 left-0 right-0 bg-surface-card/95 backdrop-blur-xl border-b border-outline/15 shadow-lg z-50 md:hidden"
        @click.self="showMobileMenu = false"
      >
        <div class="px-4 py-4 space-y-1">
          <router-link
            to="/"
            class="block px-4 py-3 rounded-xl text-sm font-medium transition-all"
            :class="route.path === '/' ? 'text-primary bg-surface-subtle' : 'text-on-surface-variant hover:bg-surface-subtle'"
            @click="showMobileMenu = false"
          >
            首页
          </router-link>
          <router-link
            v-if="userStore.isLoggedIn"
            to="/write"
            class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
            @click="showMobileMenu = false"
          >
            写文章
          </router-link>
          <router-link
            v-if="userStore.isLoggedIn"
            to="/ai-chat"
            class="block px-4 py-3 rounded-xl text-sm font-medium transition-all"
            :class="route.path === '/ai-chat' ? 'text-primary bg-surface-subtle' : 'text-on-surface-variant hover:bg-surface-subtle'"
            @click="showMobileMenu = false"
          >
            AI 助手
          </router-link>

          <div class="border-t border-outline/15 my-2" />

          <!-- Mobile user menu (when logged in) -->
          <template v-if="userStore.isLoggedIn">
            <router-link
              :to="`/user/${userStore.userInfo?.id}`"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
              @click="showMobileMenu = false"
            >
              个人主页
            </router-link>
            <router-link
              to="/favorites"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
              @click="showMobileMenu = false"
            >
              我的收藏
            </router-link>
            <router-link
              to="/history"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
              @click="showMobileMenu = false"
            >
              浏览历史
            </router-link>
            <router-link
              to="/notifications"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
              @click="showMobileMenu = false"
            >
              通知中心
            </router-link>
            <button
              class="w-full text-left px-4 py-3 rounded-xl text-sm font-medium text-red-500 hover:bg-red-50 transition-all"
              @click="handleLogout"
            >
              退出登录
            </button>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-on-surface-variant hover:bg-surface-subtle transition-all"
              @click="showMobileMenu = false"
            >
              登录
            </router-link>
            <router-link
              to="/register"
              class="block px-4 py-3 rounded-xl text-sm font-medium text-primary bg-primary-light/50 hover:bg-primary-light/80 transition-all"
              @click="showMobileMenu = false"
            >
              注册
            </router-link>
          </template>
        </div>
      </div>
    </Transition>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { onClickOutside } from '@vueuse/core'
import { Brain, Search, Bell, Sun, Moon, Menu, X } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const themeStore = useThemeStore()

const searchKeyword = ref('')
const showDropdown = ref(false)
const dropdownRef = ref(null)
const scrolled = ref(false)
const showMobileMenu = ref(false)
const showMobileSearch = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 10
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

onClickOutside(dropdownRef, () => { showDropdown.value = false })

function handleSearch() {
  showMobileMenu.value = false
  showMobileSearch.value = false
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { q: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  showDropdown.value = false
  userStore.logout()
}
</script>

<style scoped>
.dropdown-enter-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.dropdown-leave-active {
  transition: all 0.15s ease-in;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
