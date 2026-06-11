/**
 * Unified feedback utilities for user-facing messages.
 *
 * All error display goes through sanitizeErrorMessage to prevent
 * backend internals (SQL, stack traces, provider keys) from leaking.
 */

import { ElMessage } from 'element-plus'
import { sanitizeErrorMessage } from './error-sanitizer'

/** Show a success toast */
export function showSuccess(message: string): void {
  ElMessage.success(message)
}

/** Show an error toast — always sanitizes the message first */
export function showError(message: unknown): void {
  ElMessage.error(sanitizeErrorMessage(message))
}

/** Show a conflict warning (409 — optimistic lock failure) */
export function showConflict(message?: string): void {
  ElMessage.warning(message ?? '记录已更新，请刷新后重试')
}

/** Show a validation warning (422) */
export function showValidation(message?: string): void {
  ElMessage.warning(message ?? '提交数据验证失败')
}
