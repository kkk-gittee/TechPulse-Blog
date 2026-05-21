import request from './request'

export function toggleFavorite(articleId) {
  return request.post(`/api/favorite/toggle/${articleId}`)
}

export function checkFavorite(articleId) {
  return request.get(`/api/favorite/check/${articleId}`)
}

export function getFavoriteList() {
  return request.get('/api/favorite/list')
}

export function getFavoriteArticles() {
  return request.get('/api/favorite/articles')
}
