<template>
  <div class="min-h-screen bg-surface text-on-surface selection:bg-primary-light selection:text-primary overflow-x-hidden">
    <AppNavbar v-if="showNavbar" />
    <main :class="showNavbar ? 'pt-16' : ''">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <!-- Decorative Background Blobs -->
    <div class="fixed inset-0 pointer-events-none z-[-1] overflow-hidden">
      <div class="absolute top-[-10%] left-[-10%] w-[60vw] h-[60vw] rounded-full bg-primary/5 blur-[120px]" />
      <div class="absolute bottom-[-10%] right-[-10%] w-[70vw] h-[70vw] rounded-full bg-primary/10 blur-[150px]" />
    </div>

    <ToastContainer />
    <BackToTop />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { useThemeStore } from '@/stores/theme'
import { useKeyboard } from '@/composables/useKeyboard'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import ToastContainer from '@/components/common/ToastContainer.vue'
import BackToTop from '@/components/common/BackToTop.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const themeStore = useThemeStore()

const showNavbar = computed(() => !['Login', 'Register'].includes(route.name))

useKeyboard([
  {
    key: 'k',
    ctrl: true,
    handler: () => {
      const input = document.querySelector('nav input[type="text"]')
      input?.focus()
    },
  },
  {
    key: '/',
    allowInInput: false,
    handler: () => {
      const input = document.querySelector('nav input[type="text"]')
      input?.focus()
    },
  },
  {
    key: 'Escape',
    handler: () => {
      window.dispatchEvent(new CustomEvent('close-panels'))
      document.activeElement?.blur()
    },
  },
  {
    key: 'd',
    ctrl: true,
    handler: () => themeStore.toggle(),
  },
])

if (userStore.token) {
  userStore.fetchCurrentUser().then(() => {
    notificationStore.init()
  })
}
</script>
