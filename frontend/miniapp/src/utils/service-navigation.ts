const SERVICE_FILTER_INTENT_KEY = 'petcare:service-filter-intent'

interface ServiceFilterIntent {
  categoryName: string | null
}

export function openServiceCategory(categoryName: string) {
  uni.setStorageSync(SERVICE_FILTER_INTENT_KEY, { categoryName })
  uni.switchTab({ url: '/pages/services/index' })
}

export function openAllServices() {
  uni.setStorageSync(SERVICE_FILTER_INTENT_KEY, { categoryName: null })
  uni.switchTab({ url: '/pages/services/index' })
}

export function consumeServiceFilterIntent(): ServiceFilterIntent | null {
  const value = uni.getStorageSync(SERVICE_FILTER_INTENT_KEY) as unknown
  uni.removeStorageSync(SERVICE_FILTER_INTENT_KEY)

  if (!value || typeof value !== 'object' || !('categoryName' in value)) {
    return null
  }

  const categoryName = (value as { categoryName?: unknown }).categoryName
  if (categoryName !== null && typeof categoryName !== 'string') {
    return null
  }

  return { categoryName }
}
