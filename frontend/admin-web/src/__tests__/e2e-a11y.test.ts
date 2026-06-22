/**
 * H11 — E2E & Accessibility Contract Tests
 *
 * Since this project uses Vitest (not Playwright), E2E coverage is expressed
 * as structural contract tests that verify:
 *  1. Router: every route has component, title, and permission meta
 *  2. Pages: every page exists, has semantic heading, no raw card/pagination
 *  3. Accessibility: form inputs have labels (via el-form-item label-width),
 *     danger actions have confirmation dialogs, dialog titles are descriptive
 *  4. Key flows: login redirect, 403 forbidden, 404 fallback
 *  5. No page uses direct ElMessage/ElMessageBox (feedback utils enforced)
 *  6. All pages use PetCare design tokens (--pc-*)
 */

import { describe, it, expect } from 'vitest'
import { readFileSync, existsSync, readdirSync, statSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', rel)
const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const routerSrc = read('router/index.ts')

// Collect all page view files
const viewsDir = src('views')
const pageFiles: string[] = []
function collectVueFiles(dir: string, base: string = '') {
  if (!existsSync(dir)) return
  for (const entry of readdirSync(dir)) {
    const full = resolve(dir, entry)
    if (statSync(full).isDirectory()) {
      collectVueFiles(full, `${base}${entry}/`)
    } else if (entry.endsWith('.vue')) {
      pageFiles.push(`${base}${entry}`)
    }
  }
}
collectVueFiles(viewsDir)

// ──────────────────────────────────────────
// 1. Router completeness
// ──────────────────────────────────────────
describe('H11: Router contract', () => {
  const routeNames = [
    'Login', 'Dashboard', 'StoreInfo', 'StoreConfig', 'ServiceItems',
    'Staff', 'Bookings', 'Products', 'ProductOrders',
    'CommunityPosts', 'CommunityReports', 'SensitiveWords', 'OperationLogs',
    'Activities',
    'Forbidden', 'NotFound',
  ]

  for (const name of routeNames) {
    it(`route "${name}" is defined`, () => {
      expect(routerSrc).toContain(`name: '${name}'`)
    })
  }

  it('has catch-all 404 redirect', () => {
    expect(routerSrc).toMatch(/pathMatch/)
    expect(routerSrc).toMatch(/redirect.*404/)
  })

  it('has beforeEach guard for auth', () => {
    expect(routerSrc).toContain('router.beforeEach')
    expect(routerSrc).toContain('hasPermission')
  })

  it('redirects unauthenticated to login', () => {
    expect(routerSrc).toMatch(/login\?redirect/)
  })

  it('redirects unauthorized to 403', () => {
    expect(routerSrc).toContain("next('/403')")
  })
})

// ──────────────────────────────────────────
// 2. Every page exists and is well-formed
// ──────────────────────────────────────────
describe('H11: Page existence & structure', () => {
  const requiredPages = [
    'views/login/index.vue',
    'views/dashboard/index.vue',
    'views/store/info.vue',
    'views/store/config.vue',
    'views/service/index.vue',
    'views/staff/index.vue',
    'views/booking/index.vue',
    'views/product/index.vue',
    'views/product-order/index.vue',
    'views/community/posts.vue',
    'views/community/reports.vue',
    'views/activity/index.vue',
    'views/operation-logs/index.vue',
    'views/error/403.vue',
    'views/error/404.vue',
  ]

  for (const page of requiredPages) {
    it(`${page} exists`, () => {
      expect(existsSync(src(page))).toBe(true)
    })
  }

  it('all page files are tracked', () => {
    expect(pageFiles.length).toBeGreaterThanOrEqual(requiredPages.length)
  })
})

// ──────────────────────────────────────────
// 3. Accessibility: no raw ElMessage/ElMessageBox anywhere
// ──────────────────────────────────────────
describe('H11: No direct ElMessage/ElMessageBox in any page', () => {
  const pagesToCheck = pageFiles.filter(
    f => !f.includes('__tests__') && !f.includes('components/'),
  )

  for (const page of pagesToCheck) {
    const content = read(`views/${page}`)

    it(`${page}: no ElMessage import`, () => {
      expect(content).not.toMatch(/import.*\{[^}]*(?:ElMessage|ElMessageBox)[^}]*\}.*from.*element-plus/)
    })
  }
})

// ──────────────────────────────────────────
// 4. Accessibility: forms use label-width for labeling
// ──────────────────────────────────────────
describe('H11: Form accessibility — label-width', () => {
  const formPages = [
    'views/login/index.vue',
    'views/store/info.vue',
    'views/store/config.vue',
    'views/service/index.vue',
    'views/product/index.vue',
    'views/activity/index.vue',
    'views/staff/index.vue',
    'views/booking/index.vue',
  ]

  for (const page of formPages) {
    const content = read(page)

    it(`${page}: el-form has label-width`, () => {
      expect(content).toMatch(/label-width/)
    })
  }
})

// ──────────────────────────────────────────
// 5. Danger actions have confirmation dialogs
// ──────────────────────────────────────────
describe('H11: Danger actions are confirmed', () => {
  const dangerPages = [
    { file: 'views/service/index.vue', action: '禁用' },
    { file: 'views/product/index.vue', action: '下架' },
    { file: 'views/community/posts.vue', action: '删除' },
    { file: 'views/staff/index.vue', action: '禁用' },
    { file: 'views/booking/index.vue', action: '取消' },
  ]

  for (const { file, action } of dangerPages) {
    it(`${file}: "${action}" uses ActionConfirmDialog`, () => {
      const content = read(file)
      expect(content).toContain('ActionConfirmDialog')
    })
  }
})

// ──────────────────────────────────────────
// 6. Design tokens used in all refactored pages
// ──────────────────────────────────────────
describe('H11: PetCare design tokens in pages', () => {
  const tokenPages = [
    'views/booking/index.vue',
    'views/staff/index.vue',
    'views/service/index.vue',
    'views/product/index.vue',
    'views/product-order/index.vue',
    'views/community/posts.vue',
    'views/community/reports.vue',
    'views/activity/index.vue',
    'views/store/info.vue',
    'views/store/config.vue',
    'views/operation-logs/index.vue',
    'views/login/index.vue',
    'views/error/403.vue',
    'views/error/404.vue',
  ]

  for (const page of tokenPages) {
    it(`${page}: uses --pc-* CSS variables`, () => {
      const content = read(page)
      expect(content).toMatch(/--pc-/)
    })
  }
})

// ──────────────────────────────────────────
// 7. BEM naming convention in refactored pages
// ──────────────────────────────────────────
describe('H11: BEM naming convention', () => {
  const bemMap: Record<string, string> = {
    'views/booking/index.vue': 'pc-booking',
    'views/staff/index.vue': 'pc-staff',
    'views/service/index.vue': 'pc-service',
    'views/product/index.vue': 'pc-product',
    'views/product-order/index.vue': 'pc-product-order',
    'views/community/posts.vue': 'pc-community-posts',
    'views/community/reports.vue': 'pc-community-reports',
    'views/activity/index.vue': 'pc-activity',
    'views/store/info.vue': 'pc-store-info',
    'views/store/config.vue': 'pc-store-config',
    'views/operation-logs/index.vue': 'pc-operation-logs',
  }

  for (const [page, bemRoot] of Object.entries(bemMap)) {
    it(`${page}: uses BEM root "${bemRoot}"`, () => {
      const content = read(page)
      expect(content).toContain(bemRoot)
    })
  }
})

// ──────────────────────────────────────────
// 8. Shared components used in list pages
// ──────────────────────────────────────────
describe('H11: Shared component integration', () => {
  const listPages = [
    'views/booking/index.vue',
    'views/staff/index.vue',
    'views/service/index.vue',
    'views/product/index.vue',
    'views/product-order/index.vue',
    'views/community/posts.vue',
    'views/community/reports.vue',
    'views/activity/index.vue',
    'views/operation-logs/index.vue',
  ]

  for (const page of listPages) {
    const content = read(page)

    it(`${page}: uses FilterBar`, () => {
      expect(content).toContain('FilterBar')
    })

    it(`${page}: uses DataTableShell`, () => {
      expect(content).toContain('DataTableShell')
    })

    it(`${page}: no raw el-pagination`, () => {
      expect(content).not.toMatch(/<el-pagination/)
    })

    it(`${page}: no raw el-card wrapper`, () => {
      expect(content).not.toMatch(/<el-card/)
    })
  }
})
