/**
 * Marketing activity API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { ActivityItem } from '@/types/activity'

/** List active activities (paginated) */
export function getActivities(params?: PageParams): Promise<ApiResponse<PageResponse<ActivityItem>>> {
  return http.get<PageResponse<ActivityItem>>('/api/v1/activities', params as Record<string, unknown>)
}

/** Get activity detail */
export function getActivityDetail(id: string): Promise<ApiResponse<ActivityItem>> {
  return http.get<ActivityItem>(`/api/v1/activities/${id}`)
}
