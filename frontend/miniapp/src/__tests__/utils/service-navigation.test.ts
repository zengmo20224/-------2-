import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  consumeServiceFilterIntent,
  openAllServices,
  openServiceCategory,
} from '@/utils/service-navigation'
import {
  consumeCommunityTagIntent,
  openCommunityTag,
} from '@/utils/community-navigation'

const storage = new Map<string, unknown>()
const switchTab = vi.fn()

beforeEach(() => {
  storage.clear()
  switchTab.mockClear()
  vi.stubGlobal('uni', {
    setStorageSync: (key: string, value: unknown) => storage.set(key, value),
    getStorageSync: (key: string) => storage.get(key),
    removeStorageSync: (key: string) => storage.delete(key),
    switchTab,
  })
})

describe('service navigation intent', () => {
  it('opens and consumes a specific service category once', () => {
    openServiceCategory('洗护')

    expect(switchTab).toHaveBeenCalledWith({ url: '/pages/services/index' })
    expect(consumeServiceFilterIntent()).toEqual({ categoryName: '洗护' })
    expect(consumeServiceFilterIntent()).toBeNull()
  })

  it('opens the complete service catalog from the bottom navigation', () => {
    openAllServices()

    expect(consumeServiceFilterIntent()).toEqual({ categoryName: null })
  })

  it('opens a community tag through tab switch intent', () => {
    openCommunityTag('洗护经验')

    expect(switchTab).toHaveBeenCalledWith({ url: '/pages/community/index' })
    expect(consumeCommunityTagIntent()).toBe('洗护经验')
    expect(consumeCommunityTagIntent()).toBeNull()
  })
})
