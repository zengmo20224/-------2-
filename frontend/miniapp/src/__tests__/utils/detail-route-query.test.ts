import { describe, expect, it } from 'vitest'
import { readFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { normalizeRouteParam } from '@/utils/route-query'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', '..', rel)
const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const routeSensitivePages = [
  read('pages/activity/detail.vue'),
  read('pages/addresses/index.vue'),
  read('pages/addresses/edit.vue'),
  read('pages/announcement/detail.vue'),
  read('pages/booking/create.vue'),
  read('pages/booking/detail.vue'),
  read('pages/community/index.vue'),
  read('pages/products/detail.vue'),
  read('pages/services/detail.vue'),
  read('pages/community/detail.vue'),
  read('pages/order/detail.vue'),
  read('pages/pets/edit.vue'),
]

describe('detail page route query handling', () => {
  it('preserves route IDs as strings without numeric coercion', () => {
    const snowflakeId = '1894470663911096320'

    expect(normalizeRouteParam(snowflakeId)).toBe(snowflakeId)
    expect(normalizeRouteParam(['42'])).toBe('42')
    expect(normalizeRouteParam(42)).toBe('42')
    expect(normalizeRouteParam('')).toBe('')
    expect(normalizeRouteParam(undefined)).toBe('')
  })

  it('uses onLoad query parameters for mini program route-sensitive pages', () => {
    for (const page of routeSensitivePages) {
      expect(page).toContain("from '@dcloudio/uni-app'")
      expect(page).toContain('onLoad(')
      expect(page).toContain('normalizeRouteParam')
      expect(page).not.toContain('getCurrentPages()')
    }
  })
})
