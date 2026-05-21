import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const STORAGE_KEY = 'blog_theme'
const DARK_CLASS = 'dark'

function applyTheme(dark) {
  document.documentElement.classList.toggle(DARK_CLASS, dark)
}

function getInitialDark() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'dark') return true
  if (stored === 'light') return false
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(getInitialDark())

  applyTheme(isDark.value)

  let mediaQuery = null

  function bindSystemTheme() {
    if (!mediaQuery) {
      mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', onSystemThemeChange)
    }
  }

  function unbindSystemTheme() {
    if (mediaQuery) {
      mediaQuery.removeEventListener('change', onSystemThemeChange)
      mediaQuery = null
    }
  }

  function onSystemThemeChange(e) {
    if (localStorage.getItem(STORAGE_KEY) === null) {
      isDark.value = e.matches
    }
  }

  bindSystemTheme()

  function toggle() {
    isDark.value = !isDark.value
  }

  watch(isDark, (val) => {
    localStorage.setItem(STORAGE_KEY, val ? 'dark' : 'light')
    applyTheme(val)
  })

  return { isDark, toggle, unbindSystemTheme }
})
