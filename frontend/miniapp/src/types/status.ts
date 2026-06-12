/**
 * User-facing status dictionary.
 * Status values MUST match backend enum constants exactly.
 * Display labels are user-facing (differ from admin-web).
 *
 * Backend source of truth:
 *   BookingStatus:     com.petcare.booking.enums.BookingStatus
 *   ProductOrderStatus: com.petcare.product.enums.ProductOrderStatus
 *   ContentStatus:     com.petcare.community.enums.ContentStatus
 */

// ─── Booking Status (User View) ───

export const BOOKING_STATUS = {
  PENDING_CONFIRM: { label: '待确认', tagClass: 'pc-tag-warning' },
  CONFIRMED: { label: '已确认', tagClass: 'pc-tag-primary' },
  IN_SERVICE: { label: '进行中', tagClass: 'pc-tag-success' },
  COMPLETED: { label: '已完成', tagClass: 'pc-tag-info' },
  CANCELLED: { label: '已取消', tagClass: 'pc-tag-danger' },
  REJECTED: { label: '已拒绝', tagClass: 'pc-tag-danger' },
} as const

export type BookingStatus = keyof typeof BOOKING_STATUS

// ─── Product Order Status (User View) ───

export const ORDER_STATUS = {
  PENDING_CONFIRM: { label: '待确认', tagClass: 'pc-tag-warning' },
  PREPARING: { label: '备货中', tagClass: 'pc-tag-primary' },
  READY_FOR_PICKUP: { label: '待自提', tagClass: 'pc-tag-success' },
  COMPLETED: { label: '已完成', tagClass: 'pc-tag-info' },
  CANCELLED: { label: '已取消', tagClass: 'pc-tag-danger' },
  OUT_OF_STOCK: { label: '已缺货', tagClass: 'pc-tag-warning' },
} as const

export type OrderStatus = keyof typeof ORDER_STATUS

// ─── Content Status (User View) ───

export const CONTENT_STATUS = {
  PENDING_REVIEW: { label: '审核中', tagClass: 'pc-tag-warning' },
  PUBLISHED: { label: '正常', tagClass: 'pc-tag-success' },
  REJECTED: { label: '已拒绝', tagClass: 'pc-tag-danger' },
  HIDDEN: { label: '已隐藏', tagClass: 'pc-tag-info' },
  DELETED: { label: '已删除', tagClass: 'pc-tag-danger' },
} as const

export type ContentStatus = keyof typeof CONTENT_STATUS

// ─── Tag class style mapping ───

export const TAG_STYLES: Record<string, { bg: string; color: string }> = {
  'pc-tag-primary': { bg: '#DFF2ED', color: '#11796F' },
  'pc-tag-success': { bg: '#E8F5E9', color: '#2E7D32' },
  'pc-tag-warning': { bg: '#FFF0D1', color: '#F5A623' },
  'pc-tag-danger': { bg: '#FFEBEE', color: '#D84D4D' },
  'pc-tag-info': { bg: '#F5F5F5', color: '#71817D' },
}

/**
 * Get display label for a booking status value.
 * Returns the raw status string if not found in dictionary.
 */
export function getBookingStatusLabel(status: string): string {
  return BOOKING_STATUS[status as BookingStatus]?.label ?? status
}

/**
 * Get display label for an order status value.
 */
export function getOrderStatusLabel(status: string): string {
  return ORDER_STATUS[status as OrderStatus]?.label ?? status
}

/**
 * Get display label for a content status value.
 */
export function getContentStatusLabel(status: string): string {
  return CONTENT_STATUS[status as ContentStatus]?.label ?? status
}
