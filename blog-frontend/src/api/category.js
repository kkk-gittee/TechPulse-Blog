import request from './request'

export function getCategories() {
  return request.get('/api/category/list')
}

export function getCategory(id) {
  return request.get(`/api/category/${id}`)
}
