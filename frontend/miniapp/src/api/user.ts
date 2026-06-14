/**
 * User / authentication API.
 * Phone + password login is the primary authentication method.
 */

import { http } from './request'
import type { ApiResponse } from '@/types/api'

/** Login / register response */
export interface AuthResult {
  tokenType: string
  accessToken: string
  expiresInSeconds: number
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

/** Security question for password recovery */
export interface SecurityQuestion {
  id: string
  question: string
}

/** Get preset security questions for registration */
export function getPresetSecurityQuestions(): Promise<ApiResponse<string[]>> {
  return http.get<string[]>('/api/v1/auth/security-questions')
}

/** Register with phone + password + security questions */
export function register(data: {
  phone: string
  password: string
  nickname: string
  securityQuestions: { questionIndex: number; answer: string }[]
}): Promise<ApiResponse<AuthResult>> {
  return http.post<AuthResult>('/api/v1/auth/register', data as any)
}

/** Login with phone + password */
export function login(data: { phone: string; password: string }): Promise<ApiResponse<AuthResult>> {
  return http.post<AuthResult>('/api/v1/auth/login', data as any)
}

/** Get security questions for password recovery */
export function getSecurityQuestions(phone: string): Promise<ApiResponse<SecurityQuestion[]>> {
  return http.post<SecurityQuestion[]>('/api/v1/auth/forgot-password/questions', { phone })
}

/** Reset password by answering security questions */
export function resetPassword(data: {
  phone: string
  answers: { questionId: string; answer: string }[]
  newPassword: string
}): Promise<ApiResponse<void>> {
  return http.post<void>('/api/v1/auth/forgot-password/reset', data as any)
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
