import request from './request'

export function searchArticles(data) {
  return request.post('/api/search/articles', data)
}
