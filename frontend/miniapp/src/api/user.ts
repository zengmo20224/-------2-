/**
 * User / authentication API.
 * Boundary placeholder — WeChat login not yet enabled (D-006).
 * No mock login allowed.
 */

import { http } from './request'
import type { ApiResponse } from '@/types/api'

/** WeChat login — not yet implemented (D-006) */
export function wechatLogin(_code: string): Promise<ApiResponse<{ token: string }>> {
  // This function exists as an interface boundary.
  // Actual implementation requires a valid WeChat AppID.
  return Promise.resolve({
    success: false,
    error: { code: 'WECHAT_LOGIN_DISABLED', message: '微信登录尚未启用' },
  })
}

/** Get current user profile — placeholder */
export function getUserProfile(): Promise<ApiResponse<unknown>> {
  return http.get('/api/user/profile')
}
