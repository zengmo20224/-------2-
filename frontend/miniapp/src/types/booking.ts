/**
 * Booking-related types for user-facing miniapp.
 * Backend source: com.petcare.booking.dto.*
 */

/** Booking as shown in list/detail */
export interface BookingItem {
  id: string
  serviceId: string
  serviceName: string
  staffName?: string
  bookingDate: string
  timeSlot: string
  mode: string
  status: string
  petName?: string
  address?: string
  totalPrice?: number
  createdAt: string
}
