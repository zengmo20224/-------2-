import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types (match backend DTOs exactly) ───

export interface Announcement {
  id: number
  title: string
  content: string
  status: string // PUBLISHED | DRAFT
  sort: number
  createTime: string
  updateTime: string
}

export interface AnnouncementCreateParams {
  title: string
  content: string
  status?: string
  sort?: number
}

export interface AnnouncementQueryParams extends PageParams {
  status?: string
}

// ─── API Functions ───

/** GET /api/v1/admin/announcements */
export const getAnnouncementList = (params: AnnouncementQueryParams) => {
  return request.get<PageResponse<Announcement>>('/v1/admin/announcements', { params })
}

/** POST /api/v1/admin/announcements */
export const createAnnouncement = (data: AnnouncementCreateParams) => {
  return request.post<Announcement>('/v1/admin/announcements', data)
}

/** PUT /api/v1/admin/announcements/{id} */
export const updateAnnouncement = (id: number, data: AnnouncementCreateParams) => {
  return request.put<Announcement>(`/v1/admin/announcements/${id}`, data)
}

/** DELETE /api/v1/admin/announcements/{id} */
export const deleteAnnouncement = (id: number) => {
  return request.delete<void>(`/v1/admin/announcements/${id}`)
}
