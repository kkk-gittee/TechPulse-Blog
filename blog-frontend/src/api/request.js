import axios from 'axios'
import { getToken, setToken, removeToken, getRefreshToken } from '@/utils/storage'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

// ─── Token refresh state (prevents concurrent refresh calls) ───
let isRefreshing = false
let refreshQueue = []

function resolveRefreshQueue(newToken) {
  refreshQueue.forEach(({ resolve }) => resolve(newToken))
  refreshQueue = []
}

function rejectRefreshQueue(error) {
  refreshQueue.forEach(({ reject }) => reject(error))
  refreshQueue = []
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null

  try {
    const { data } = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL || ''}/api/user/refresh`,
      { refreshToken },
      { headers: { 'Content-Type': 'application/json' } }
    )
    if (data.code === 200 && data.data?.accessToken) {
      setToken(data.data.accessToken)
      return data.data.accessToken
    }
  } catch { /* refresh failed */ }
  return null
}

// ─── Request interceptor ──────────────────────────────────

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ─── Response interceptor ──────────────────────────────────

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    const bizError = new Error(res.message || '请求失败')
    bizError.code = res.code
    return Promise.reject(bizError)
  },
  async (error) => {
    if (!error.response) {
      const netError = new Error('网络连接失败，请检查网络设置')
      netError.code = 0
      return Promise.reject(netError)
    }

    const { status, data, config } = error.response

    // ── 401: Try silent token refresh ──
    if (status === 401 && !config._retry) {
      config._retry = true

      if (!isRefreshing) {
        isRefreshing = true
        try {
          const newToken = await refreshAccessToken()
          if (newToken) {
            isRefreshing = false
            resolveRefreshQueue(newToken)
            // Retry original request with new token
            config.headers.Authorization = `Bearer ${newToken}`
            return request(config)
          }
        } catch { /* fall through to redirect */ }
        isRefreshing = false
        rejectRefreshQueue(new Error('Token 刷新失败'))
      } else {
        // Another request is already refreshing — queue this one
        return new Promise((resolve, reject) => {
          refreshQueue.push({
            resolve: (token) => {
              config.headers.Authorization = `Bearer ${token}`
              resolve(request(config))
            },
            reject
          })
        })
      }

      // Refresh failed — clear tokens and redirect
      removeToken()
      router.push('/login')
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }

    // ── 403: Forbidden ──
    if (status === 403) {
      const httpError = new Error(data?.message || '权限不足')
      httpError.code = 403
      httpError.status = 403
      return Promise.reject(httpError)
    }

    const httpError = new Error(data?.message || error.message || '请求失败')
    httpError.code = data?.code || status
    httpError.status = status
    return Promise.reject(httpError)
  }
)

export default request
