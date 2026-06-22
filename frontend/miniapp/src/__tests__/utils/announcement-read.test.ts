import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  hasUnreadAnnouncements,
  isAnnouncementRead,
  markAllAnnouncementsRead,
  markAnnouncementRead,
} from '@/utils/announcement-read'

const storage = new Map<string, unknown>()

beforeEach(() => {
  storage.clear()
  vi.stubGlobal('uni', {
    setStorageSync: (key: string, value: unknown) => storage.set(key, value),
    getStorageSync: (key: string) => storage.get(key),
  })
})

describe('announcement read state', () => {
  it('marks one announcement as read', () => {
    expect(isAnnouncementRead('1001')).toBe(false)
    markAnnouncementRead('1001')
    expect(isAnnouncementRead('1001')).toBe(true)
    expect(hasUnreadAnnouncements(['1001', '1002'])).toBe(true)
  })

  it('marks all current announcements as read while leaving new ones unread', () => {
    markAllAnnouncementsRead(['1001', '1002'])

    expect(hasUnreadAnnouncements(['1001', '1002'])).toBe(false)
    expect(hasUnreadAnnouncements(['1001', '1002', '1003'])).toBe(true)
  })
})
