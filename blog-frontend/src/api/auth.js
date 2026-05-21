import request from './request'

export function login(data) {
  return request.post('/api/user/login', data)
}

export function register(data) {
  return request.post('/api/user/register', data)
}

export function getCurrentUser() {
  return request.get('/api/user/info')
}

export function getPublicUser(userId) {
  return request.get(`/api/user/public/${userId}`)
}

export function updateUser(data) {
  return request.put('/api/user/update', data)
}
