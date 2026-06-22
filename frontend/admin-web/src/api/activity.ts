import request from '../utils/request'
import type { PageParams, PageResponse } from '../types/api'

export type ActivityId = string | number

export interface Activity {
  id: ActivityId
  title: string
  activityType: string
  description: string | null
  coverUrl: string | null
  startTime: string | null
  endTime: string | null
  status: string
}

export interface ActivityDetail extends Activity {
  productIds: ActivityId[]
  serviceItemIds: ActivityId[]
}

export interface ActivityUpsertParams {
  title: string
  activityType: string
  description?: string
  coverUrl?: string
  startTime?: string | null
  endTime?: string | null
  productIds: ActivityId[]
  serviceItemIds: ActivityId[]
}

export interface ActivityQueryParams extends PageParams {
  status?: string
}

export const getActivityList = (params: ActivityQueryParams) => {
  return request.get<PageResponse<Activity>>('/v1/admin/activities', { params })
}

export const getActivityDetail = (id: ActivityId) => {
  return request.get<ActivityDetail>(`/v1/admin/activities/${id}`)
}

export const createActivity = (data: ActivityUpsertParams) => {
  return request.post<ActivityDetail>('/v1/admin/activities', data)
}

export const updateActivity = (id: ActivityId, data: ActivityUpsertParams) => {
  return request.put<ActivityDetail>(`/v1/admin/activities/${id}`, data)
}

export const updateActivityStatus = (id: ActivityId, status: string) => {
  return request.post<void>(`/v1/admin/activities/${id}/status`, { status })
}

export const deleteActivity = (id: ActivityId) => {
  return request.delete<void>(`/v1/admin/activities/${id}`)
}
