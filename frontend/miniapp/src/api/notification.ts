/**
 * Notification and announcement API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { AnnouncementItem, NotificationItem, UnreadCount } from '@/types/notification'

/** List published announcements (public) */
export function getAnnouncements(limit?: number): Promise<ApiResponse<AnnouncementItem[]>> {
  const params: Record<string, unknown> = {}
  if (limit) params.limit = limit
  return http.get<AnnouncementItem[]>('/api/v1/announcements', params)
}

/** Get single announcement detail (public) */
export function getAnnouncementDetail(id: string): Promise<ApiResponse<AnnouncementItem>> {
  return http.get<AnnouncementItem>(`/api/v1/announcements/${id}`)
}

/** List current user notifications (requires auth) */
export function getNotifications(params?: PageParams): Promise<ApiResponse<PageResponse<NotificationItem>>> {
  return http.get<PageResponse<NotificationItem>>('/api/v1/user/notifications', params as Record<string, unknown>)
}

/** Get unread notification count (requires auth) */
export function getUnreadCount(): Promise<ApiResponse<UnreadCount>> {
  return http.get<UnreadCount>('/api/v1/user/notifications/unread-count')
}

/** Mark a single notification as read */
export function markNotificationRead(id: string): Promise<ApiResponse<void>> {
  return http.post<void>(`/api/v1/user/notifications/${id}/read`)
}

/** Mark all notifications as read */
export function markAllNotificationsRead(): Promise<ApiResponse<void>> {
  return http.post<void>('/api/v1/user/notifications/read-all')
}
