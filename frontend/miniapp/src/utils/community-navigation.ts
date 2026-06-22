const COMMUNITY_TAG_INTENT_KEY = 'petcare:community-tag-intent'

export function openCommunityTag(tag: string) {
  uni.setStorageSync(COMMUNITY_TAG_INTENT_KEY, tag)
  uni.switchTab({ url: '/pages/community/index' })
}

export function consumeCommunityTagIntent(): string | null {
  const value = uni.getStorageSync(COMMUNITY_TAG_INTENT_KEY) as unknown
  uni.removeStorageSync(COMMUNITY_TAG_INTENT_KEY)

  return typeof value === 'string' && value.trim() ? value : null
}
