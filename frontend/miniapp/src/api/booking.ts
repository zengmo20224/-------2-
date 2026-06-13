/**
 * Booking API.
 * Requires authentication (user JWT via test-login or future H5 login).
 * Availability query is public.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { BookingItem, BookingAvailability } from '@/types/booking'

/** Query available time slots (public, no auth required) */
export function getAvailability(params: {
  storeId: string
  serviceItemId: string
  bookingDate: string
  serviceMode: string
}): Promise<ApiResponse<BookingAvailability>> {
  return http.get<BookingAvailability>('/api/v1/bookings/availability', params as Record<string, unknown>)
}

/** Create a new booking (requires auth) */
export function createBooking(data: {
  storeId: string
  serviceItemId: string
  petId?: string
  serviceMode: string
  bookingDate: string
  startTime: string
  addressId?: string
  contactName: string
  contactPhone: string
  paymentMethod: string
  remark?: string
}): Promise<ApiResponse<BookingItem>> {
  return http.post<BookingItem>('/api/v1/bookings', data as any)
}

/** List current user's bookings (requires auth) */
export function getMyBookings(params?: PageParams): Promise<ApiResponse<PageResponse<BookingItem>>> {
  return http.get<PageResponse<BookingItem>>('/api/v1/bookings/my', params as Record<string, unknown>)
}

/** Get booking detail (requires auth) */
export function getBookingDetail(id: string): Promise<ApiResponse<BookingItem>> {
  return http.get<BookingItem>(`/api/v1/bookings/${id}`)
}

/** Cancel a booking (requires auth) */
export function cancelBooking(id: string, reason?: string): Promise<ApiResponse<BookingItem>> {
  return http.post<BookingItem>(`/api/v1/bookings/${id}/cancel`, reason ? { reason } : undefined)
}
