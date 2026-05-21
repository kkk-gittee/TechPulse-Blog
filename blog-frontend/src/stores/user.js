import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setRefreshToken } from '@/utils/storage'
import { login as apiLogin, register as apiRegister, getCurrentUser, updateUser } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const data = await apiLogin({ username, password })
    token.value = data.accessToken || data.token
    setToken(data.accessToken || data.token)
    if (data.refreshToken) setRefreshToken(data.refreshToken)
    await fetchCurrentUser()
  }

  async function register(userData) {
    await apiRegister(userData)
  }

  async function fetchCurrentUser() {
    try {
      const data = await getCurrentUser()
      userInfo.value = data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
    router.push('/login')
  }

  async function updateProfile(userData) {
    await updateUser(userData)
    await fetchCurrentUser()
  }

  return { token, userInfo, isLoggedIn, login, register, fetchCurrentUser, logout, updateProfile }
})
