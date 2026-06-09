import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types (match backend DTOs exactly) ───

export interface ServiceItem {
  id: number
  categoryId: number
  name: string
  serviceMode: string // STORE | HOME | BOTH
  price: number
  durationMinutes: number
  petType: string // DOG | CAT | ALL
  petSize: string // SMALL | MEDIUM | LARGE | ALL
  needAddress: boolean
  needPet: boolean
  description: string
  coverUrl: string
  status: string // ACTIVE | DISABLED
  sort: number
}

export interface ServiceItemCreateParams {
  categoryId: number
  name: string
  serviceMode: string
  price: number
  durationMinutes: number
  petType?: string
  petSize?: string
  needAddress: boolean
  needPet: boolean
  description?: string
  coverUrl?: string
  sort?: number
}

export interface ServiceItemQueryParams extends PageParams {
  name?: string
  status?: string
}

// ─── API Functions ───

/** GET /api/v1/admin/service-items */
export const getServiceItems = (params: ServiceItemQueryParams) => {
  return request.get<PageResponse<ServiceItem>>('/v1/admin/service-items', { params })
}

/** POST /api/v1/admin/service-items */
export const createServiceItem = (data: ServiceItemCreateParams) => {
  return request.post<ServiceItem>('/v1/admin/service-items', data)
}

/** PUT /api/v1/admin/service-items/{id} */
export const updateServiceItem = (id: number, data: ServiceItemCreateParams) => {
  return request.put<ServiceItem>(`/v1/admin/service-items/${id}`, data)
}

/** POST /api/v1/admin/service-items/{id}/disable */
export const disableServiceItem = (id: number) => {
  return request.post<ServiceItem>(`/v1/admin/service-items/${id}/disable`)
}
