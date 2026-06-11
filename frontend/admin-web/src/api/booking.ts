import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types ───

export interface Booking {
  id: number
  bookingNo: string
  userId: number
  petId: number | null
  storeId: number
  serviceItemId: number
  staffId: number | null
  serviceMode: string
  bookingDate: string
  startTime: string
  endTime: string
  addressId: number | null
  distanceKm: number | null
  contactName: string
  contactPhone: string
  price: number
  paymentMethod: string
  paymentStatus: string
  status: string
  remark: string | null
  merchantRemark: string | null
  createTime: string
}

export interface BookingQueryParams extends PageParams {
  status?: string
  bookingDate?: string
}

export interface BookingConfirmParams {
  merchantRemark?: string
}

export interface BookingRejectParams {
  reason: string
}

export interface BookingReassignParams {
  newStaffId: number
}

// ─── API Functions ───

export const getBookingList = (params: BookingQueryParams) => {
  return request.get<PageResponse<Booking>>('/v1/admin/bookings', { params })
}

export const getBookingDetail = (id: number) => {
  return request.get<Booking>(`/v1/admin/bookings/${id}`)
}

export const confirmBooking = (id: number, data?: BookingConfirmParams) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/confirm`, data)
}

export const rejectBooking = (id: number, data: BookingRejectParams) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/reject`, data)
}

export const startBooking = (id: number) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/start`)
}

export const completeBooking = (id: number) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/complete`)
}

export const cancelBooking = (id: number, data?: { reason?: string }) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/cancel`, data)
}

export const reassignBooking = (id: number, data: BookingReassignParams) => {
  return request.post<Booking>(`/v1/admin/bookings/${id}/reassign`, data)
}
