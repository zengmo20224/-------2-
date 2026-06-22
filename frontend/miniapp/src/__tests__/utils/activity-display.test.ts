import { describe, expect, it } from 'vitest'
import { readFileSync } from 'fs'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', '..', rel)
const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const homePage = read('pages/home/index.vue')
const activityListPage = read('pages/activity/index.vue')
const activityDetailPage = read('pages/activity/detail.vue')
const activityTypes = read('types/activity.ts')

describe('user-facing marketing activity display', () => {
  it('places store activities between featured products and community posts on home', () => {
    const productsIndex = homePage.indexOf('精选好物')
    const activitiesIndex = homePage.indexOf('门店活动')
    const communityIndex = homePage.indexOf('社区动态')

    expect(productsIndex).toBeGreaterThan(-1)
    expect(activitiesIndex).toBeGreaterThan(productsIndex)
    expect(communityIndex).toBeGreaterThan(activitiesIndex)
  })

  it('supports activity cover images and clickable product/service cards', () => {
    expect(activityTypes).toContain('coverUrl')
    expect(activityTypes).toContain('ActivityProductCard')
    expect(activityTypes).toContain('ActivityServiceCard')

    expect(activityListPage).toContain('coverUrl')
    expect(activityListPage).toContain('formatActivityTime')

    expect(activityDetailPage).toContain('goProductDetail')
    expect(activityDetailPage).toContain('/pages/products/detail?id=')
    expect(activityDetailPage).toContain('goServiceDetail')
    expect(activityDetailPage).toContain('/pages/services/detail?id=')
    expect(activityDetailPage).toContain('activity.value?.products ?? []')
    expect(activityDetailPage).toContain('activity.value?.services ?? []')
  })
})
