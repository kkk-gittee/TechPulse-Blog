import request from './request'

export function toggleFollow(userId) {
  return request.post(`/api/follow/toggle/${userId}`)
}

export function checkFollow(userId) {
  return request.get(`/api/follow/check/${userId}`)
}

export function getFollowStats(userId) {
  return request.get(`/api/follow/stats/${userId}`)
}
