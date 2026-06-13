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

/** Create a post (requires auth) */
export function createPost(data: {
  title: string
  content: string
  topicId?: string
  petId?: string
}): Promise<ApiResponse<unknown>> {
  return http.post<unknown>('/api/v1/posts', data as any)
}

/** Create a comment (requires auth) */
export function createComment(postId: string, content: string): Promise<ApiResponse<unknown>> {
  return http.post<unknown>(`/api/v1/posts/${postId}/comments`, { content } as any)
}

/** Like a post (requires auth, idempotent) */
export function likePost(postId: string): Promise<ApiResponse<void>> {
  return http.post<void>(`/api/v1/posts/${postId}/like`)
}

/** Unlike a post (requires auth, idempotent) */
export function unlikePost(postId: string): Promise<ApiResponse<void>> {
  return http.delete<void>(`/api/v1/posts/${postId}/like`)
}

/** Favorite a post (requires auth, idempotent) */
export function favoritePost(postId: string): Promise<ApiResponse<void>> {
  return http.post<void>(`/api/v1/posts/${postId}/favorite`)
}

/** Unfavorite a post (requires auth, idempotent) */
export function unfavoritePost(postId: string): Promise<ApiResponse<void>> {
  return http.delete<void>(`/api/v1/posts/${postId}/favorite`)
}
