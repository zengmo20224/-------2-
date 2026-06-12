/**
 * Booking API.
 * Note: Most booking endpoints require authentication.
 * These are placeholder definitions — actual calls will fail until auth is ready.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { BookingItem } from '@/types/booking'

/** List user bookings (requires auth) */
export function getMyBookings(params?: PageParams): Promise<ApiResponse<PageResponse<BookingItem>>> {
  return http.get<PageResponse<BookingItem>>('/api/user/bookings', params as Record<string, unknown>)
}

/** Get booking detail (requires auth) */
export function getBookingDetail(id: string): Promise<ApiResponse<BookingItem>> {
  return http.get<BookingItem>(`/api/user/bookings/${id}`)
}
