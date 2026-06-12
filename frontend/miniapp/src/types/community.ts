/**
 * Community-related types for user-facing miniapp.
 * Backend source: com.petcare.community.dto.*
 */

/** Post as shown in feed/detail */
export interface PostItem {
  id: string
  authorName?: string
  content: string
  imageUrl?: string
  likeCount: number
  commentCount: number
  status: string
  createdAt: string
}
