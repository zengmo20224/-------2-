import { describe, expect, it } from 'vitest'
import { existsSync, readFileSync } from 'fs'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', rel)
const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const routerSource = read('router/index.ts')
const layoutSource = read('layout/index.vue')

describe('admin marketing activity management', () => {
  it('registers a dedicated marketing activity route and menu permission', () => {
    expect(routerSource).toMatch(/path:\s*'activities'/)
    expect(routerSource).toMatch(/permission:\s*'marketing:activity:read'/)
    expect(layoutSource).toContain('营销活动')
    expect(layoutSource).toContain("hasPermission('marketing:activity:read')")
  })

  it('has an activity API matching the backend contract', () => {
    expect(existsSync(src('api/activity.ts'))).toBe(true)
    const apiSource = read('api/activity.ts')

    expect(apiSource).toContain('/v1/admin/activities')
    expect(apiSource).toContain('createActivity')
    expect(apiSource).toContain('updateActivity')
    expect(apiSource).toContain('updateActivityStatus')
    expect(apiSource).toContain('deleteActivity')
    expect(apiSource).toContain('coverUrl')
    expect(apiSource).toContain('productIds')
    expect(apiSource).toContain('serviceItemIds')
  })

  it('provides a Chinese management page with cover upload and association selection', () => {
    expect(existsSync(src('views/activity/index.vue'))).toBe(true)
    const pageSource = read('views/activity/index.vue')

    expect(pageSource).toContain('活动管理')
    expect(pageSource).toContain('导入封面图')
    expect(pageSource).toContain('uploadCatalogImage')
    expect(pageSource).toContain('form.coverUrl')
    expect(pageSource).toContain('form.productIds')
    expect(pageSource).toContain('form.serviceItemIds')
    expect(pageSource).toContain('ACTIVITY_STATUS')
    expect(pageSource).not.toMatch(/import.*ElMessageBox.*from.*element-plus/)
  })
})
