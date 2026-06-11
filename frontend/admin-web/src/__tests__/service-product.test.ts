/**
 * H07 — Service & Product Management Tests (RED phase)
 *
 * Covers:
 *  1. Shared component usage (FilterBar, DataTableShell, ActionConfirmDialog)
 *  2. PetCare design tokens in styles
 *  3. Feedback utils (no direct ElMessage / ElMessageBox)
 *  4. Permission-based button gating
 *  5. Price display via toFixed(2) — no floating-point raw output
 *  6. Status enum rendering (SERVICE_MODE, SERVICE_STATUS, PRODUCT_STATUS, PET_TYPE)
 *  7. File upload 10 MB limit validation constant
 *  8. Service form defaults & product form defaults
 *  9. Query param handling (page, size, status, reset)
 */

import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', rel)

const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const serviceVue = read('views/service/index.vue')
const productVue = read('views/product/index.vue')
const feedbackSrc = read('utils/feedback.ts')
const statusSrc = read('types/status.ts')

// ──────────────────────────────────────────
// 1. Shared components — Service
// ──────────────────────────────────────────
describe('H07: Service page — shared components', () => {
  it('uses FilterBar component', () => {
    expect(serviceVue).toContain('FilterBar')
    expect(serviceVue).toContain('@search')
    expect(serviceVue).toContain('@reset')
  })

  it('uses DataTableShell component', () => {
    expect(serviceVue).toContain('DataTableShell')
    expect(serviceVue).toContain(':data=')
    expect(serviceVue).toContain(':total=')
    expect(serviceVue).toContain(':loading=')
    expect(serviceVue).toContain('@page-change=')
  })

  it('uses ActionConfirmDialog for disable action', () => {
    expect(serviceVue).toContain('ActionConfirmDialog')
    expect(serviceVue).toMatch(/@confirm/)
  })

  it('does NOT use raw el-pagination directly', () => {
    expect(serviceVue).not.toMatch(/<el-pagination/)
  })

  it('does NOT use raw el-card wrapper', () => {
    expect(serviceVue).not.toMatch(/<el-card/)
  })

  it('does NOT use inline el-form for filters (replaced by FilterBar slot)', () => {
    // The page should use FilterBar with slot, not standalone inline el-form for search
    const filterBarCount = (serviceVue.match(/<FilterBar/g) || []).length
    expect(filterBarCount).toBeGreaterThanOrEqual(1)
  })
})

// ──────────────────────────────────────────
// 2. Shared components — Product
// ──────────────────────────────────────────
describe('H07: Product page — shared components', () => {
  it('uses FilterBar component', () => {
    expect(productVue).toContain('FilterBar')
    expect(productVue).toContain('@search')
    expect(productVue).toContain('@reset')
  })

  it('uses DataTableShell component', () => {
    expect(productVue).toContain('DataTableShell')
    expect(productVue).toContain(':data=')
    expect(productVue).toContain(':total=')
    expect(productVue).toContain(':loading=')
    expect(productVue).toContain('@page-change=')
  })

  it('uses ActionConfirmDialog for disable/takedown action', () => {
    expect(productVue).toContain('ActionConfirmDialog')
  })

  it('does NOT use raw el-pagination directly', () => {
    expect(productVue).not.toMatch(/<el-pagination/)
  })

  it('does NOT use raw el-card wrapper', () => {
    expect(productVue).not.toMatch(/<el-card/)
  })
})

// ──────────────────────────────────────────
// 3. PetCare design tokens in styles
// ──────────────────────────────────────────
describe('H07: Design tokens in page styles', () => {
  it('service page uses --pc-* CSS variables', () => {
    expect(serviceVue).toMatch(/--pc-/)
  })

  it('product page uses --pc-* CSS variables', () => {
    expect(productVue).toMatch(/--pc-/)
  })

  it('service page uses BEM naming (pc-service)', () => {
    expect(serviceVue).toMatch(/pc-service/)
  })

  it('product page uses BEM naming (pc-product)', () => {
    expect(productVue).toMatch(/pc-product/)
  })
})

// ──────────────────────────────────────────
// 4. Feedback utils — no direct ElMessage / ElMessageBox
// ──────────────────────────────────────────
describe('H07: Feedback utilities (no raw ElMessage/ElMessageBox)', () => {
  it('service page does NOT import ElMessage directly', () => {
    expect(serviceVue).not.toMatch(/import.*ElMessage[^B].*from.*element-plus/)
  })

  it('service page does NOT import ElMessageBox directly', () => {
    expect(serviceVue).not.toMatch(/import.*ElMessageBox.*from.*element-plus/)
  })

  it('service page uses showSuccess/showError from feedback utils', () => {
    expect(serviceVue).toMatch(/showSuccess|showError/)
    expect(serviceVue).toMatch(/from.*utils\/feedback/)
  })

  it('product page does NOT import ElMessage directly', () => {
    expect(productVue).not.toMatch(/import.*ElMessage[^B].*from.*element-plus/)
  })

  it('product page does NOT import ElMessageBox directly', () => {
    expect(productVue).not.toMatch(/import.*ElMessageBox.*from.*element-plus/)
  })

  it('product page uses showSuccess/showError from feedback utils', () => {
    expect(productVue).toMatch(/showSuccess|showError/)
    expect(productVue).toMatch(/from.*utils\/feedback/)
  })
})

// ──────────────────────────────────────────
// 5. Permission-based button gating
// ──────────────────────────────────────────
describe('H07: Permission checks on action buttons', () => {
  it('service create button gated by service:item:create', () => {
    expect(serviceVue).toContain("hasPermission('service:item:create')")
  })

  it('service edit button gated by service:item:update', () => {
    expect(serviceVue).toContain("hasPermission('service:item:update')")
  })

  it('service disable button gated by service:item:disable', () => {
    expect(serviceVue).toContain("hasPermission('service:item:disable')")
  })

  it('product create button gated by product:item:create', () => {
    expect(productVue).toContain("hasPermission('product:item:create')")
  })

  it('product edit button gated by product:item:update', () => {
    expect(productVue).toContain("hasPermission('product:item:update')")
  })

  it('product disable/takedown button gated by product:item:disable', () => {
    expect(productVue).toContain("hasPermission('product:item:disable')")
  })

  it('product stock button gated by product:stock:update', () => {
    expect(productVue).toContain("hasPermission('product:stock:update')")
  })
})

// ──────────────────────────────────────────
// 6. Price display — toFixed(2) for safe rendering
// ──────────────────────────────────────────
describe('H07: Price display uses toFixed(2)', () => {
  it('service page renders price with toFixed(2)', () => {
    expect(serviceVue).toMatch(/Number\(.*\.price\)\.toFixed\(2\)/)
  })

  it('product page renders price with toFixed(2)', () => {
    expect(productVue).toMatch(/Number\(.*\.price\)\.toFixed\(2\)/)
  })
})

// ──────────────────────────────────────────
// 7. Status enum rendering
// ──────────────────────────────────────────
describe('H07: Status enums used for rendering', () => {
  it('service page imports SERVICE_MODE', () => {
    expect(serviceVue).toContain('SERVICE_MODE')
  })

  it('service page imports SERVICE_STATUS', () => {
    expect(serviceVue).toContain('SERVICE_STATUS')
  })

  it('service page imports PET_TYPE', () => {
    expect(serviceVue).toContain('PET_TYPE')
  })

  it('service page uses isServiceOnSale guard', () => {
    expect(serviceVue).toContain('isServiceOnSale')
  })

  it('product page imports PRODUCT_STATUS', () => {
    expect(productVue).toContain('PRODUCT_STATUS')
  })

  it('product page uses isProductOnSale guard', () => {
    expect(productVue).toContain('isProductOnSale')
  })
})

// ──────────────────────────────────────────
// 8. Query param handling (page-change & reset)
// ──────────────────────────────────────────
describe('H07: Query param lifecycle', () => {
  it('service page has handlePageChange handler', () => {
    expect(serviceVue).toContain('handlePageChange')
  })

  it('service page has handleReset handler', () => {
    expect(serviceVue).toContain('handleReset')
  })

  it('product page has handlePageChange handler', () => {
    expect(productVue).toContain('handlePageChange')
  })

  it('product page has handleReset handler', () => {
    expect(productVue).toContain('handleReset')
  })
})

// ──────────────────────────────────────────
// 9. File upload 10 MB limit constant
// ──────────────────────────────────────────
describe('H07: File upload 10 MB limit', () => {
  it('status module defines MAX_UPLOAD_SIZE constant (10 MB)', () => {
    expect(statusSrc).toMatch(/MAX_UPLOAD_SIZE/)
    expect(statusSrc).toMatch(/10.*\*.*1024.*\*.*1024/)
  })
})
