/**
 * Centralized status dictionaries for display labels, colors, and allowed actions.
 * These values MUST match backend enum constants exactly.
 * Frontend only controls display; backend controls actual state transitions.
 */

// ─── Store Status ───

export const STORE_STATUS = {
  OPEN: { label: '营业中', color: 'success' },
  CLOSED: { label: '已休息', color: 'info' },
} as const

export type StoreStatus = keyof typeof STORE_STATUS

// ─── Service Item ───

export const SERVICE_MODE = {
  STORE: { label: '到店', color: 'success' },
  HOME: { label: '上门', color: 'warning' },
  BOTH: { label: '到店/上门', color: 'primary' },
} as const

export type ServiceMode = keyof typeof SERVICE_MODE

export const SERVICE_STATUS = {
  ACTIVE: { label: '启用', color: 'success' },
  DISABLED: { label: '已禁用', color: 'info' },
} as const

export type ServiceStatus = keyof typeof SERVICE_STATUS

export const PET_TYPE = {
  DOG: '犬类',
  CAT: '猫类',
  ALL: '不限',
} as const

export type PetType = keyof typeof PET_TYPE

export const PET_SIZE = {
  SMALL: '小型',
  MEDIUM: '中型',
  LARGE: '大型',
  ALL: '不限',
} as const

export type PetSize = keyof typeof PET_SIZE

// ─── Staff ───

export const STAFF_ROLE = {
  GROOMER: { label: '美容师', color: 'primary' },
  WALKER: { label: '遛狗员', color: 'success' },
  FEEDER: { label: '喂养员', color: 'warning' },
  MANAGER: { label: '店长', color: 'danger' },
} as const

export type StaffRole = keyof typeof STAFF_ROLE

export const STAFF_STATUS = {
  ACTIVE: { label: '在职', color: 'success' },
  DISABLED: { label: '已停用', color: 'info' },
} as const

export type StaffStatus = keyof typeof STAFF_STATUS

export const SCHEDULE_STATUS = {
  AVAILABLE: { label: '可用', color: 'success' },
  UNAVAILABLE: { label: '不可用', color: 'danger' },
} as const

export type ScheduleStatus = keyof typeof SCHEDULE_STATUS

// ─── Booking ───

export const BOOKING_STATUS = {
  PENDING: { label: '待确认', color: 'warning' },
  CONFIRMED: { label: '已确认', color: 'primary' },
  IN_PROGRESS: { label: '进行中', color: 'success' },
  COMPLETED: { label: '已完成', color: 'info' },
  CANCELLED: { label: '已取消', color: 'danger' },
  REJECTED: { label: '已拒绝', color: 'danger' },
} as const

export type BookingStatus = keyof typeof BOOKING_STATUS

export const PAYMENT_STATUS = {
  PENDING: { label: '待支付', color: 'warning' },
  PAID: { label: '已支付', color: 'success' },
  REFUNDED: { label: '已退款', color: 'info' },
  CANCELLED: { label: '已取消', color: 'danger' },
} as const

export type PaymentStatus = keyof typeof PAYMENT_STATUS

// ─── Product ───

export const PRODUCT_STATUS = {
  ACTIVE: { label: '上架', color: 'success' },
  DISABLED: { label: '已下架', color: 'info' },
} as const

export type ProductStatus = keyof typeof PRODUCT_STATUS

export const PRODUCT_ORDER_STATUS = {
  PENDING: { label: '待确认', color: 'warning' },
  CONFIRMED: { label: '已确认', color: 'primary' },
  READY: { label: '待自提', color: 'success' },
  COMPLETED: { label: '已完成', color: 'info' },
  CANCELLED: { label: '已取消', color: 'danger' },
} as const

export type ProductOrderStatus = keyof typeof PRODUCT_ORDER_STATUS

export const PICKUP_STATUS = {
  PENDING: { label: '待自提', color: 'warning' },
  PICKED_UP: { label: '已自提', color: 'success' },
} as const

export type PickupStatus = keyof typeof PICKUP_STATUS

// ─── Community ───

export const POST_STATUS = {
  DRAFT: { label: '草稿', color: 'info' },
  PENDING_REVIEW: { label: '待审核', color: 'warning' },
  PUBLISHED: { label: '已发布', color: 'success' },
  REJECTED: { label: '已拒绝', color: 'danger' },
  HIDDEN: { label: '已隐藏', color: 'info' },
  DELETED: { label: '已删除', color: 'danger' },
} as const

export type PostStatus = keyof typeof POST_STATUS

export const REPORT_HANDLE_RESULT = {
  PROCESSED: { label: '已处理', color: 'success' },
  IGNORED: { label: '已忽略', color: 'info' },
} as const

export type ReportHandleResult = keyof typeof REPORT_HANDLE_RESULT

// ─── Sensitive Word ───

export const SENSITIVE_WORD_STATUS = {
  ACTIVE: { label: '启用', color: 'success' },
  DISABLED: { label: '已禁用', color: 'info' },
} as const

export type SensitiveWordStatus = keyof typeof SENSITIVE_WORD_STATUS
