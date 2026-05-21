import request from './request'

export function getHotTags(limit = 20) {
  return request.get('/api/tag/hot', { params: { limit } })
}

export function getAllTags() {
  return request.get('/api/tag/list')
}
