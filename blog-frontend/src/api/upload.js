import request from './request'

export function uploadImage(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/upload/image', formData, {
    // Let browser set Content-Type with multipart boundary automatically
    onUploadProgress: onProgress
  })
}
