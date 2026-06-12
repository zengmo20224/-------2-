/**
 * Community / posts API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { PostItem } from '@/types/community'

/** List published posts (paginated) */
export function getPosts(params?: PageParams): Promise<ApiResponse<PageResponse<PostItem>>> {
  return http.get<PageResponse<PostItem>>('/api/user/community/posts', params as Record<string, unknown>)
}

/** Get post detail */
export function getPostDetail(id: string): Promise<ApiResponse<PostItem>> {
  return http.get<PostItem>(`/api/user/community/posts/${id}`)
}
