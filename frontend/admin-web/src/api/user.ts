import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types (match backend DTOs exactly) ───

export interface UserView {
  id: number
  nickname: string | null
  phone: string | null
  avatarUrl: string | null
  gender: number | null
  status: string // ACTIVE | BANNED
  realName: string | null
  banDescription: string | null // e.g. "剩余6天" / "永久封禁" (only when BANNED)
  lastLoginTime: string | null
  createTime: string
}

export interface UserBanResult {
  id: number
  status: string
  banLevel: number
  banDays: number | null // null = permanent
  banUntil: string | null
  description: string | null
}

export interface UserBanParams {
  reason?: string
}

export interface UserQueryParams extends PageParams {
  status?: string
  keyword?: string
}

// Booking / order history (subset of fields used for admin display)
export interface UserBooking {
  id: number
  bookingNo: string
  serviceItemName: string | null
  serviceMode: string
  bookingDate: string
  startTime: string
  endTime: string
  price: number
  status: string
  createTime: string
}

export interface UserOrder {
  id: number
  orderNo: string
  totalAmount: number
  deliveryMethod: string
  paymentStatus: string
  status: string
  contactName: string
  createTime: string
}

// ─── API Functions ───

/** GET /api/v1/admin/users */
export const getUserList = (params: UserQueryParams) => {
  return request.get<PageResponse<UserView>>('/v1/admin/users', { params })
}

/** GET /api/v1/admin/users/{id} */
export const getUserDetail = (id: number) => {
  return request.get<UserView>(`/v1/admin/users/${id}`)
}

/** POST /api/v1/admin/users/{id}/ban — returns computed ban level/duration */
export const banUser = (id: number, data?: UserBanParams) => {
  return request.post<UserBanResult>(`/v1/admin/users/${id}/ban`, data)
}

/** POST /api/v1/admin/users/{id}/unban */
export const unbanUser = (id: number) => {
  return request.post<UserView>(`/v1/admin/users/${id}/unban`)
}

/** GET /api/v1/admin/users/{id}/bookings */
export const getUserBookings = (id: number, params: PageParams) => {
  return request.get<PageResponse<UserBooking>>(`/v1/admin/users/${id}/bookings`, { params })
}

/** GET /api/v1/admin/users/{id}/orders */
export const getUserOrders = (id: number, params: PageParams) => {
  return request.get<PageResponse<UserOrder>>(`/v1/admin/users/${id}/orders`, { params })
}
