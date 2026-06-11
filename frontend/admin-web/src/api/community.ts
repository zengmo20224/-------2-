import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types ───

export interface Post {
  id: number
  userId: number
  petId: number | null
  topicId: number | null
  title: string
  content: string
  status: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  publishTime: string | null
  createTime: string
}

export interface Comment {
  id: number
  postId: number
  userId: number
  parentId: number | null
  content: string
  status: string
  likeCount: number
  createTime: string
}

export interface PostReport {
  id: number
  postId: number
  reporterId: number
  reasonType: string | null
  reason: string
  status: string
  handleResult: string | null
  handlerId: number | null
  handleTime: string | null
  createTime: string
}

export interface AdminReviewParams {
  remark?: string
}

export interface AdminReportHandleParams {
  handleResult: string // PROCESSED | IGNORED
  hidePost?: boolean
  handleRemark?: string
}

export interface PostQueryParams extends PageParams {
  status?: string
}

export interface CommentQueryParams extends PageParams {
  status?: string
}

export interface ReportQueryParams extends PageParams {
  status?: string
}

// ─── Post API ───

export const getPostList = (params: PostQueryParams) => {
  return request.get<PageResponse<Post>>('/v1/admin/community/posts', { params })
}

export const getPostDetail = (id: number) => {
  return request.get<Post>(`/v1/admin/community/posts/${id}`)
}

export const approvePost = (id: number, data?: AdminReviewParams) => {
  return request.post<Post>(`/v1/admin/community/posts/${id}/approve`, data)
}

export const rejectPost = (id: number, data?: AdminReviewParams) => {
  return request.post<Post>(`/v1/admin/community/posts/${id}/reject`, data)
}

export const hidePost = (id: number) => {
  return request.post<Post>(`/v1/admin/community/posts/${id}/hide`)
}

export const deletePost = (id: number) => {
  return request.post<void>(`/v1/admin/community/posts/${id}/delete`)
}

// ─── Comment API ───

export const getCommentList = (params: CommentQueryParams) => {
  return request.get<PageResponse<Comment>>('/v1/admin/community/comments', { params })
}

export const approveComment = (id: number, data?: AdminReviewParams) => {
  return request.post<Comment>(`/v1/admin/community/comments/${id}/approve`, data)
}

export const rejectComment = (id: number, data?: AdminReviewParams) => {
  return request.post<Comment>(`/v1/admin/community/comments/${id}/reject`, data)
}

export const hideComment = (id: number) => {
  return request.post<Comment>(`/v1/admin/community/comments/${id}/hide`)
}

export const deleteComment = (id: number) => {
  return request.post<void>(`/v1/admin/community/comments/${id}/delete`)
}

// ─── Report API ───

export const getReportList = (params: ReportQueryParams) => {
  return request.get<PageResponse<PostReport>>('/v1/admin/community/reports', { params })
}

export const handleReport = (id: number, data: AdminReportHandleParams) => {
  return request.post<void>(`/v1/admin/community/reports/${id}/handle`, data)
}
