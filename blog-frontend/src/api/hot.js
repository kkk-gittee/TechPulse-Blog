import request from './request'

export function getHotArticles(limit = 10) {
  return request.get('/api/hot/list', { params: { limit } })
}

export function clearHotArticles() {
  return request.delete('/api/hot/clear')
}
