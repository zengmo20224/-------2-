import { describe, it, expect } from 'vitest'
import {
  formatPrice,
  formatYuan,
  formatDate,
  formatTime,
  formatRelativeTime,
  formatDuration,
} from '@/utils/format'

describe('formatPrice', () => {
  it('formats cents to yuan', () => {
    expect(formatPrice(9900)).toBe('¥99.00')
    expect(formatPrice(100)).toBe('¥1.00')
    expect(formatPrice(0)).toBe('¥0.00')
  })

  it('returns -- for null/undefined', () => {
    expect(formatPrice(null)).toBe('--')
    expect(formatPrice(undefined)).toBe('--')
  })
})

describe('formatYuan', () => {
  it('formats yuan directly', () => {
    expect(formatYuan(99)).toBe('¥99.00')
    expect(formatYuan(0)).toBe('¥0.00')
    expect(formatYuan(9.9)).toBe('¥9.90')
  })

  it('returns -- for null/undefined', () => {
    expect(formatYuan(null)).toBe('--')
    expect(formatYuan(undefined)).toBe('--')
  })
})

describe('formatDate', () => {
  it('formats YYYY-MM-DD to Chinese', () => {
    expect(formatDate('2024-06-15')).toBe('6月15日')
    expect(formatDate('2024-01-05')).toBe('1月5日')
  })

  it('returns empty for empty input', () => {
    expect(formatDate('')).toBe('')
    expect(formatDate(null)).toBe('')
    expect(formatDate(undefined)).toBe('')
  })

  it('returns raw string for non-matching format', () => {
    expect(formatDate('not-a-date')).toBe('not-a-date')
  })
})

describe('formatTime', () => {
  it('formats HH:mm', () => {
    expect(formatTime('9:30')).toBe('09:30')
    expect(formatTime('14:00')).toBe('14:00')
  })

  it('returns empty for empty input', () => {
    expect(formatTime('')).toBe('')
    expect(formatTime(null)).toBe('')
  })
})

describe('formatRelativeTime', () => {
  it('returns "刚刚" for very recent times', () => {
    const now = new Date().toISOString()
    expect(formatRelativeTime(now)).toBe('刚刚')
  })

  it('returns empty for empty input', () => {
    expect(formatRelativeTime('')).toBe('')
    expect(formatRelativeTime(null)).toBe('')
  })

  it('returns formatted date for old dates', () => {
    expect(formatRelativeTime('2020-01-01T00:00:00Z')).toBe('1月1日')
  })
})

describe('formatDuration', () => {
  it('formats minutes only', () => {
    expect(formatDuration(30)).toBe('30分钟')
    expect(formatDuration(45)).toBe('45分钟')
  })

  it('formats hours and minutes', () => {
    expect(formatDuration(90)).toBe('1小时30分钟')
    expect(formatDuration(120)).toBe('2小时')
  })

  it('returns empty for null/undefined', () => {
    expect(formatDuration(null)).toBe('')
    expect(formatDuration(undefined)).toBe('')
  })
})
