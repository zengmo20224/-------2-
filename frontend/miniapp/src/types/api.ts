/**
 * Unified API response types matching backend ApiResponse<T> structure.
 * Copied from admin-web — no shared package to avoid platform API leakage.
 */

/** Field-level validation error detail */
export interface ApiFieldError {
  field: string
  message: string
}

/** Structured API error returned by backend */
export interface ApiError {
  code: string
  message: string
  details?: ApiFieldError[]
}

/** Standard backend response envelope */
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: ApiError
  meta?: unknown
}

/** Paginated list response from backend */
export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

/** Common pagination query parameters */
export interface PageParams {
  page?: number
  size?: number
}
