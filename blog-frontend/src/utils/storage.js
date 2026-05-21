const ACCESS_TOKEN_KEY = 'blog_access_token'
const REFRESH_TOKEN_KEY = 'blog_refresh_token'

function safeGet(key) {
  try { return localStorage.getItem(key) } catch { return null }
}

function safeSet(key, value) {
  try { localStorage.setItem(key, value) } catch { /* quota exceeded or private mode */ }
}

function safeRemove(key) {
  try { localStorage.removeItem(key) } catch { /* ignore */ }
}

export function getToken() {
  return safeGet(ACCESS_TOKEN_KEY)
}

export function setToken(token) {
  safeSet(ACCESS_TOKEN_KEY, token)
}

export function removeToken() {
  safeRemove(ACCESS_TOKEN_KEY)
  safeRemove(REFRESH_TOKEN_KEY)
}

export function getRefreshToken() {
  return safeGet(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token) {
  safeSet(REFRESH_TOKEN_KEY, token)
}
