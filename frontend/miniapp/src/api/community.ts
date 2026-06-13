/**
 * Community / posts API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { PostItem, PostDetail, CommentItem } from '@/types/community'

/** List published posts (paginated) */
export function getPosts(params?: PageParams & { topicId?: string }): Promise<ApiResponse<PageResponse<PostItem>>> {
  return http.get<PageResponse<PostItem>>('/api/v1/posts', params as Record<string, unknown>)
}

/** Get published post detail */
export function getPostDetail(id: string): Promise<ApiResponse<PostDetail>> {
  return http.get<PostDetail>(`/api/v1/posts/${id}`)
}

/** List published comments for a post (paginated) */
export function getPostComments(postId: string, params?: PageParams): Promise<ApiResponse<PageResponse<CommentItem>>> {
  return http.get<PageResponse<CommentItem>>(`/api/v1/posts/${postId}/comments`, params as Record<string, unknown>)
}
