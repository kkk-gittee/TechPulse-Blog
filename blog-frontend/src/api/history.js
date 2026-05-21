import request from './request'

export function getHistory(params) {
  return request.get('/api/history/list', { params })
}

export function clearHistory() {
  return request.delete('/api/history/clear')
}
