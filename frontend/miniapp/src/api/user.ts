/**
 * User / authentication API.
 * WeChat login deferred per H5-first strategy.
 * Test login available in test profile only.
 */

import { http } from './request'
import type { ApiResponse } from '@/types/api'

/** Test login response */
export interface TestLoginResult {
  tokenType: string
  accessToken: string
  expiresIn: number
  user: { id: string; nickname: string }
}

/** User profile */
export interface UserProfile {
  id: string
  nickname: string
  avatarUrl: string | null
  phone: string | null
  gender: number | null
  status: string
}

/** Test login (only available in test profile) */
export function testLogin(phone: string): Promise<ApiResponse<TestLoginResult>> {
  return http.post<TestLoginResult>('/api/v1/auth/test-login', { phone })
}

/** WeChat login — not yet implemented */
export function wechatLogin(_code: string): Promise<ApiResponse<{ token: string }>> {
  return Promise.resolve({
    success: false,
    error: { code: 'WECHAT_LOGIN_DISABLED', message: '微信登录尚未启用' },
  })
}

/** Get current user profile */
export function getUserProfile(): Promise<ApiResponse<UserProfile>> {
  return http.get<UserProfile>('/api/v1/user/profile')
}

/** Update current user profile */
export function updateUserProfile(data: { nickname?: string; avatarUrl?: string; gender?: number }): Promise<ApiResponse<UserProfile>> {
  return http.put<UserProfile>('/api/v1/user/profile', data as any)
}
