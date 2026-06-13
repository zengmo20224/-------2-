/**
 * Booking API.
 * Requires authentication (user JWT via test-login or future H5 login).
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { BookingItem } from '@/types/booking'

/** List user bookings (requires auth) */
export function getMyBookings(params?: PageParams): Promise<ApiResponse<PageResponse<BookingItem>>> {
  return http.get<PageResponse<BookingItem>>('/api/v1/bookings/my', params as Record<string, unknown>)
}

/** Get booking detail (requires auth) */
export function getBookingDetail(id: string): Promise<ApiResponse<BookingItem>> {
  return http.get<BookingItem>(`/api/v1/bookings/${id}`)
}
