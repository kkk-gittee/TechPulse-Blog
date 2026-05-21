import request from './request'

export function getArticleList(params) {
  return request.get('/api/article/list', { params })
}

export function getArticleDetail(id) {
  return request.get(`/api/article/detail/${id}`)
}

export function createArticle(data) {
  return request.post('/api/article/create', data)
}

export function updateArticle(id, data) {
  return request.put(`/api/article/update/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/api/article/delete/${id}`)
}

export function toggleLike(id) {
  return request.post(`/api/article/like/${id}`)
}

export function getUserArticles(userId, params) {
  return request.get(`/api/article/user/${userId}`, { params })
}
