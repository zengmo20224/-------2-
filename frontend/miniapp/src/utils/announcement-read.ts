const ANNOUNCEMENT_READ_IDS_KEY = 'petcare:announcement-read-ids'

function getReadIds(): string[] {
  const value = uni.getStorageSync(ANNOUNCEMENT_READ_IDS_KEY) as unknown
  if (!Array.isArray(value)) return []
  return value.filter((id): id is string => typeof id === 'string')
}

function saveReadIds(ids: string[]) {
  uni.setStorageSync(ANNOUNCEMENT_READ_IDS_KEY, [...new Set(ids)])
}

export function isAnnouncementRead(id: string): boolean {
  return getReadIds().includes(id)
}

export function markAnnouncementRead(id: string) {
  saveReadIds([...getReadIds(), id])
}

export function markAllAnnouncementsRead(ids: string[]) {
  saveReadIds([...getReadIds(), ...ids])
}

export function hasUnreadAnnouncements(ids: string[]): boolean {
  const readIds = new Set(getReadIds())
  return ids.some(id => !readIds.has(id))
}
