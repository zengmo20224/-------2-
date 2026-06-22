/**
 * Notification and announcement types.
 */

/** Public announcement */
export interface AnnouncementItem {
  id: string
  title: string
  content: string
  sort: number
  createTime: string
}

/** User notification item */
export interface NotificationItem {
  id: string
  type: string
  postId: string | null
  commentId: string | null
  content: string | null
  isRead: boolean
  actorName: string | null
  actorAvatar: string | null
  createTime: string
}

/** Unread count */
export interface UnreadCount {
  count: number
}
