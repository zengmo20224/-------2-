/**
 * Community-related types for the user-facing H5 app.
 * Backend source: com.petcare.community.dto.PublicPostSummaryResponse / PublicPostDetailResponse / PublicCommentResponse
 */

/** Post summary as returned by public list API */
export interface PostItem {
  id: string
  topicId: string
  title: string
  content: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  publishTime: string
  createTime: string
}

/** Post detail as returned by public detail API */
export interface PostDetail {
  id: string
  topicId: string
  title: string
  content: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  publishTime: string
  createTime: string
  imageUrls: string[]
}

/** Public comment as returned by comment list API */
export interface CommentItem {
  id: string
  parentId: string | null
  content: string
  likeCount: number
  createTime: string
}
