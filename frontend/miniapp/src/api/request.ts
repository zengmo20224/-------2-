/**
 * HTTP request layer using uni.request.
 * Adapts admin-web Axios patterns to uni-app runtime.
 */

import type { ApiResponse } from '@/types/api'
import { sanitizeErrorMessage } from '@/utils/error-sanitizer'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

type RequestData = Exclude<UniNamespace.RequestOptions['data'], undefined>
type RequestMethod = Exclude<UniNamespace.RequestOptions['method'], undefined>

interface RequestOptions {
  url: string
  method?: RequestMethod
  data?: RequestData
  params?: Record<string, unknown>
  header?: Record<string, string>
}

/** Build query string from params object */
function buildQueryString(params: Record<string, unknown>): string {
  const parts: string[] = []
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null) {
      parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    }
  }
  return parts.length > 0 ? `?${parts.join('&')}` : ''
}

/** Get stored user token */
function getToken(): string | null {
  try {
    return uni.getStorageSync('user_token') || null
  } catch {
    return null
  }
}

/** Clear stored token on auth failure */
function clearToken(): void {
  try {
    uni.removeStorageSync('user_token')
  } catch {
    // ignore storage errors
  }
}

/** Show a toast message */
function showToast(title: string, icon: 'none' | 'success' | 'error' | 'loading' = 'none'): void {
  uni.showToast({ title, icon, duration: 2000 })
}

/**
 * Typed request wrapper around uni.request.
 * Returns ApiResponse<T> directly (unwrapped from HTTP response).
 */
export function request<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  const token = getToken()
  const queryString = options.params ? buildQueryString(options.params) : ''
  const fullUrl = `${BASE_URL}${options.url}${queryString}`

  return new Promise((resolve) => {
    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success: (res) => {
        const statusCode = res.statusCode
        const body = res.data as ApiResponse<T>

        if (statusCode === 401) {
          clearToken()
          showToast('登录已过期，请重新登录')
          resolve({ success: false, error: { code: 'UNAUTHORIZED', message: '登录已过期' } })
          return
        }

        if (statusCode === 403) {
          resolve({ success: false, error: { code: 'FORBIDDEN', message: '无权访问' } })
          return
        }

        if (statusCode === 409) {
          const msg = body?.error?.message || '数据状态冲突，请刷新后重试'
          showToast(msg)
          resolve({ success: false, error: { code: 'CONFLICT', message: msg } })
          return
        }

        if (statusCode === 422) {
          const msg = body?.error?.message || '提交数据验证失败'
          showToast(msg)
          resolve({ success: false, error: { code: 'UNPROCESSABLE', message: msg } })
          return
        }

        if (statusCode >= 400) {
          const msg = body?.error?.message ? sanitizeErrorMessage(body.error.message) : '请求失败'
          showToast(msg)
          resolve({ success: false, error: { code: `HTTP_${statusCode}`, message: msg } })
          return
        }

        // 2xx success — check business-level error
        if (body && body.success === false) {
          const msg = body.error?.message ? sanitizeErrorMessage(body.error.message) : '操作失败'
          showToast(msg)
          resolve({ success: false, error: { code: body.error?.code || 'BIZ_ERROR', message: msg, details: body.error?.details } })
          return
        }

        resolve(body || { success: true })
      },
      fail: (err) => {
        showToast('网络连接失败')
        resolve({ success: false, error: { code: 'NETWORK_ERROR', message: '网络连接失败' } })
      },
    })
  })
}

/** Convenience methods */
export const http = {
  get<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'GET', params })
  },

  post<T>(url: string, data?: RequestData): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'POST', data })
  },

  put<T>(url: string, data?: RequestData): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'PUT', data })
  },

  delete<T>(url: string): Promise<ApiResponse<T>> {
    return request<T>({ url, method: 'DELETE' })
  },
}

export default http
