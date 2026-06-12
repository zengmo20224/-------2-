/**
 * Service catalog API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { ServiceItem, ServiceCategory } from '@/types/service'

/** List service categories */
export function getServiceCategories(): Promise<ApiResponse<ServiceCategory[]>> {
  return http.get<ServiceCategory[]>('/api/user/services/categories')
}

/** List service items (paginated) */
export function getServiceItems(params?: PageParams & { categoryId?: string; mode?: string }): Promise<ApiResponse<PageResponse<ServiceItem>>> {
  return http.get<PageResponse<ServiceItem>>('/api/user/services/items', params as Record<string, unknown>)
}

/** Get service item detail */
export function getServiceDetail(id: string): Promise<ApiResponse<ServiceItem>> {
  return http.get<ServiceItem>(`/api/user/services/items/${id}`)
}
