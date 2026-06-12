/**
 * Formatting utilities for user-facing display.
 */

/** Format price in yuan (分 → 元) */
export function formatPrice(priceInCents?: number | null): string {
  if (priceInCents == null) return '--'
  return `¥${(priceInCents / 100).toFixed(2)}`
}

/** Format price in yuan directly (元 → 元) */
export function formatYuan(price?: number | null): string {
  if (price == null) return '--'
  return `¥${price.toFixed(2)}`
}

/** Format date string for display (YYYY-MM-DD → MM月DD日) */
export function formatDate(dateStr?: string | null): string {
  if (!dateStr) return ''
  const match = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (!match) return dateStr
  return `${parseInt(match[2])}月${parseInt(match[3])}日`
}

/** Format time for display (HH:mm) */
export function formatTime(timeStr?: string | null): string {
  if (!timeStr) return ''
  const match = timeStr.match(/^(\d{1,2}):(\d{2})/)
  if (!match) return timeStr
  return `${match[1].padStart(2, '0')}:${match[2]}`
}

/** Format relative time (e.g. "3分钟前", "2小时前", "昨天") */
export function formatRelativeTime(dateStr?: string | null): string {
  if (!dateStr) return ''
  const now = Date.now()
  const target = new Date(dateStr).getTime()
  const diff = now - target

  if (diff < 0) return '刚刚'

  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`

  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`

  return formatDate(dateStr)
}

/** Format duration in minutes for display */
export function formatDuration(minutes?: number | null): string {
  if (minutes == null) return ''
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}
