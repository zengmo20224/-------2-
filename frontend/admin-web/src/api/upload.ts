import request from '../utils/request'

export interface UploadResponse {
  url: string
}

export const uploadCatalogImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<UploadResponse>('/v1/upload', formData)
}
