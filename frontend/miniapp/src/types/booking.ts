/**
 * Booking-related types for the user-facing H5 app.
 * Backend source: com.petcare.booking.dto.BookingResponse / BookingAvailabilityResponse
 */

/** Booking as returned by booking API */
export interface BookingItem {
  id: string
  bookingNo: string
  userId: string
  petId: string | null
  storeId: string
  serviceItemId: string
  serviceItemName: string | null
  staffId: string | null
  serviceMode: string
  bookingDate: string
  startTime: string
  endTime: string
  addressId: string | null
  distanceKm: number | null
  contactName: string
  contactPhone: string
  price: number | null
  paymentMethod: string | null
  paymentStatus: string
  status: string
  remark: string | null
  merchantRemark: string | null
  createTime: string
}

/** Available time slot */
export interface BookingSlot {
  startTime: string
  endTime: string
  availableStaffCount: number
}

/** Availability query response */
export interface BookingAvailability {
  storeId: string
  serviceItemId: string
  bookingDate: string
  serviceMode: string
  durationMinutes: number
  timeSlotMinutes: number
  slots: BookingSlot[]
}
