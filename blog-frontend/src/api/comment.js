import request from './request'

export function addComment(data) {
  return request.post('/api/comment/add', data)
}

export function getComments(articleId) {
  return request.get(`/api/comment/list/${articleId}`)
}

export function deleteComment(id) {
  return request.delete(`/api/comment/delete/${id}`)
}

export function toggleCommentLike(commentId) {
  return request.post(`/api/comment/like/${commentId}`)
}
