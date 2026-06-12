import { describe, it, expect } from 'vitest'
import {
  BOOKING_STATUS,
  ORDER_STATUS,
  CONTENT_STATUS,
  TAG_STYLES,
  getBookingStatusLabel,
  getOrderStatusLabel,
  getContentStatusLabel,
} from '@/types/status'

describe('BOOKING_STATUS', () => {
  it('has all expected status keys', () => {
    expect(Object.keys(BOOKING_STATUS)).toEqual(
      expect.arrayContaining([
        'PENDING_CONFIRM',
        'CONFIRMED',
        'IN_SERVICE',
        'COMPLETED',
        'CANCELLED',
        'REJECTED',
      ]),
    )
  })

  it('each status has label and tagClass', () => {
    for (const [, value] of Object.entries(BOOKING_STATUS)) {
      expect(value).toHaveProperty('label')
      expect(value).toHaveProperty('tagClass')
      expect(typeof value.label).toBe('string')
      expect(typeof value.tagClass).toBe('string')
    }
  })
})

describe('ORDER_STATUS', () => {
  it('has all expected status keys', () => {
    expect(Object.keys(ORDER_STATUS)).toEqual(
      expect.arrayContaining([
        'PENDING_CONFIRM',
        'PREPARING',
        'READY_FOR_PICKUP',
        'COMPLETED',
        'CANCELLED',
        'OUT_OF_STOCK',
      ]),
    )
  })
})

describe('CONTENT_STATUS', () => {
  it('has all expected status keys', () => {
    expect(Object.keys(CONTENT_STATUS)).toEqual(
      expect.arrayContaining([
        'PENDING_REVIEW',
        'PUBLISHED',
        'REJECTED',
        'HIDDEN',
        'DELETED',
      ]),
    )
  })
})

describe('TAG_STYLES', () => {
  it('has bg and color for each tag class', () => {
    for (const [, style] of Object.entries(TAG_STYLES)) {
      expect(style).toHaveProperty('bg')
      expect(style).toHaveProperty('color')
      expect(style.bg).toMatch(/^#/)
      expect(style.color).toMatch(/^#/)
    }
  })
})

describe('getBookingStatusLabel', () => {
  it('returns correct labels for known statuses', () => {
    expect(getBookingStatusLabel('PENDING_CONFIRM')).toBe('待确认')
    expect(getBookingStatusLabel('COMPLETED')).toBe('已完成')
    expect(getBookingStatusLabel('CANCELLED')).toBe('已取消')
  })

  it('returns raw status for unknown values', () => {
    expect(getBookingStatusLabel('UNKNOWN')).toBe('UNKNOWN')
  })
})

describe('getOrderStatusLabel', () => {
  it('returns correct labels', () => {
    expect(getOrderStatusLabel('PENDING_CONFIRM')).toBe('待确认')
    expect(getOrderStatusLabel('READY_FOR_PICKUP')).toBe('待自提')
  })

  it('returns raw status for unknown values', () => {
    expect(getOrderStatusLabel('UNKNOWN')).toBe('UNKNOWN')
  })
})

describe('getContentStatusLabel', () => {
  it('returns correct labels', () => {
    expect(getContentStatusLabel('PUBLISHED')).toBe('正常')
    expect(getContentStatusLabel('PENDING_REVIEW')).toBe('审核中')
  })

  it('returns raw status for unknown values', () => {
    expect(getContentStatusLabel('UNKNOWN')).toBe('UNKNOWN')
  })
})
