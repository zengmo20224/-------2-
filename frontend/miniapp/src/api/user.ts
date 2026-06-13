/**
 * User / authentication API.
 * WeChat login deferred per H5-first strategy.
 * Test login available in test profile only.
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

/** Get current user profile */
export function getUserProfile(): Promise<ApiResponse<unknown>> {
  return http.get('/api/v1/user/profile')
}
