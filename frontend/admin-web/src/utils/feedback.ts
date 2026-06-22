/**
 * Unified feedback utilities for user-facing messages.
 *
 * All error display goes through sanitizeErrorMessage to prevent
 * backend internals (SQL, stack traces, provider keys) from leaking.
 */

import { ElMessage, ElMessageBox } from 'element-plus'
import { sanitizeErrorMessage } from './error-sanitizer'

const AXIOS_STATUS_MESSAGE = /Request failed with status code (\d{3})/i

type ConfirmOptions = {
  type?: 'success' | 'warning' | 'info' | 'error'
  confirmButtonText?: string
  cancelButtonText?: string
  distinguishCancelAndClose?: boolean
}

type ResponseLike = {
  response?: {
    status?: number
    data?: {
      error?: { message?: string }
      message?: string
    }
  }
}

function messageForStatus(status?: number, fallback = '操作失败，请稍后重试'): string {
  switch (status) {
    case 400:
      return '请求参数不合法，请检查填写内容'
    case 401:
      return '登录已过期，请重新登录'
    case 403:
      return '无权访问该功能'
    case 409:
      return '数据状态冲突，请刷新后重试'
    case 422:
      return '提交数据验证失败'
    case 500:
      return '服务内部错误，请稍后重试'
    default:
      return fallback
  }
}

/** Convert unknown errors, including Axios errors, into safe user-facing text. */
export function getUserErrorMessage(error: unknown, fallback = '操作失败，请稍后重试'): string {
  const response = typeof error === 'object' && error !== null
    ? (error as ResponseLike).response
    : undefined

  if (response) {
    const backendMessage = response.data?.error?.message || response.data?.message
    return backendMessage
      ? sanitizeErrorMessage(backendMessage)
      : messageForStatus(response.status, fallback)
  }

  const rawMessage = error instanceof Error ? error.message : error
  if (typeof rawMessage === 'string') {
    const match = rawMessage.match(AXIOS_STATUS_MESSAGE)
    if (match) {
      return messageForStatus(Number(match[1]), fallback)
    }
    return sanitizeErrorMessage(rawMessage || fallback)
  }

  return sanitizeErrorMessage(fallback)
}

/** Show a success toast */
export function showSuccess(message: string): void {
  ElMessage.success(message)
}

/** Show an error toast — always sanitizes the message first */
export function showError(message: unknown, fallback?: string): void {
  ElMessage.error(getUserErrorMessage(message, fallback))
}

/** Show a conflict warning (409 — optimistic lock failure) */
export function showConflict(message?: string): void {
  ElMessage.warning(message ? sanitizeErrorMessage(message) : '记录已更新，请刷新后重试')
}

/** Show a validation warning (422) */
export function showValidation(message?: string): void {
  ElMessage.warning(message ? sanitizeErrorMessage(message) : '提交数据验证失败')
}

/** Show a confirmation dialog through the shared feedback surface */
export function showConfirm(message: string, title = '提示', options: ConfirmOptions = {}) {
  return ElMessageBox.confirm(message, title, {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    ...options,
  })
}
