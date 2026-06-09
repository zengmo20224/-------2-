import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types ───

export interface SensitiveWord {
  id: number
  word: string
  category: string
  level: number
  status: string
  createTime: string
}

export interface SensitiveWordCreateParams {
  word: string
  category?: string
  level: number
}

export interface SensitiveWordUpdateParams {
  word?: string
  category?: string
  level?: number
}

export interface SensitiveWordQueryParams extends PageParams {
  status?: string
}

// ─── API Functions ───

export const getSensitiveWords = (params: SensitiveWordQueryParams) => {
  return request.get<PageResponse<SensitiveWord>>('/v1/admin/moderation/sensitive-words', { params })
}

export const createSensitiveWord = (data: SensitiveWordCreateParams) => {
  return request.post<SensitiveWord>('/v1/admin/moderation/sensitive-words', data)
}

export const updateSensitiveWord = (id: number, data: SensitiveWordUpdateParams) => {
  return request.patch<SensitiveWord>(`/v1/admin/moderation/sensitive-words/${id}`, data)
}

export const disableSensitiveWord = (id: number) => {
  return request.post<void>(`/v1/admin/moderation/sensitive-words/${id}/disable`)
}
