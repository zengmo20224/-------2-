/**
 * H10 — Store Config, Store Info & Operation Logs Tests (RED phase)
 *
 * Covers:
 *  1. Store info: no el-card, PetCare tokens, BEM, feedback utils, STORE_STATUS enum
 *  2. Store config: no el-card, PetCare tokens, BEM, feedback utils, permission gating
 *  3. Operation logs: new page with FilterBar, DataTableShell, PetCare tokens
 *  4. All pages: no direct ElMessage/ElMessageBox
 *  5. Operation logs: module filter, query param lifecycle
 *  6. Result column uses tag with color coding
 */

import { describe, it, expect } from 'vitest'
import { readFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', rel)
const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const storeInfoVue = read('views/store/info.vue')
const storeConfigVue = read('views/store/config.vue')
const operationLogsVue = read('views/operation-logs/index.vue')

// ──────────────────────────────────────────
// 1. Store Info page
// ──────────────────────────────────────────
describe('H10: Store Info page', () => {
  it('does NOT use raw el-card wrapper', () => {
    expect(storeInfoVue).not.toMatch(/<el-card/)
  })

  it('uses PetCare design tokens (--pc-*)', () => {
    expect(storeInfoVue).toMatch(/--pc-/)
  })

  it('uses BEM naming (pc-store-info)', () => {
    expect(storeInfoVue).toMatch(/pc-store-info/)
  })

  it('does NOT import ElMessage directly', () => {
    expect(storeInfoVue).not.toMatch(/import.*ElMessage[^B].*from.*element-plus/)
  })

  it('imports showSuccess/showError from feedback utils', () => {
    expect(storeInfoVue).toMatch(/showSuccess|showError/)
    expect(storeInfoVue).toMatch(/from.*utils\/feedback/)
  })

  it('gates save by store:info:update permission', () => {
    expect(storeInfoVue).toContain("hasPermission('store:info:update')")
  })

  it('uses STORE_STATUS enum for status dropdown (not hardcoded)', () => {
    expect(storeInfoVue).not.toMatch(/<el-option label="营业中" value="OPEN"/)
    expect(storeInfoVue).not.toMatch(/<el-option label="已休息" value="CLOSED"/)
    expect(storeInfoVue).toMatch(/STORE_STATUS/)
  })

  it('has a page title with BEM class', () => {
    expect(storeInfoVue).toMatch(/pc-store-info__title/)
  })
})

// ──────────────────────────────────────────
// 2. Store Config page
// ──────────────────────────────────────────
describe('H10: Store Config page', () => {
  it('does NOT use raw el-card wrapper', () => {
    expect(storeConfigVue).not.toMatch(/<el-card/)
  })

  it('uses PetCare design tokens (--pc-*)', () => {
    expect(storeConfigVue).toMatch(/--pc-/)
  })

  it('uses BEM naming (pc-store-config)', () => {
    expect(storeConfigVue).toMatch(/pc-store-config/)
  })

  it('does NOT import ElMessage directly', () => {
    expect(storeConfigVue).not.toMatch(/import.*ElMessage[^B].*from.*element-plus/)
  })

  it('imports showSuccess/showError from feedback utils', () => {
    expect(storeConfigVue).toMatch(/showSuccess|showError/)
    expect(storeConfigVue).toMatch(/from.*utils\/feedback/)
  })

  it('gates save by store:config:update permission', () => {
    expect(storeConfigVue).toContain("hasPermission('store:config:update')")
  })

  it('has a page title with BEM class', () => {
    expect(storeConfigVue).toMatch(/pc-store-config__title/)
  })
})

// ──────────────────────────────────────────
// 3. Operation Logs page
// ──────────────────────────────────────────
describe('H10: Operation Logs page', () => {
  it('file exists', () => {
    expect(existsSync(src('views/operation-logs/index.vue'))).toBe(true)
  })

  it('uses FilterBar component', () => {
    expect(operationLogsVue).toContain('FilterBar')
    expect(operationLogsVue).toContain('@search')
    expect(operationLogsVue).toContain('@reset')
  })

  it('uses DataTableShell component', () => {
    expect(operationLogsVue).toContain('DataTableShell')
    expect(operationLogsVue).toContain(':data=')
    expect(operationLogsVue).toContain(':total=')
    expect(operationLogsVue).toContain('@page-change=')
  })

  it('does NOT use raw el-pagination', () => {
    expect(operationLogsVue).not.toMatch(/<el-pagination/)
  })

  it('uses PetCare design tokens (--pc-*)', () => {
    expect(operationLogsVue).toMatch(/--pc-/)
  })

  it('uses BEM naming (pc-operation-logs)', () => {
    expect(operationLogsVue).toMatch(/pc-operation-logs/)
  })

  it('does NOT import ElMessage directly', () => {
    expect(operationLogsVue).not.toMatch(/import.*ElMessage[^B].*from.*element-plus/)
  })
})

// ──────────────────────────────────────────
// 4. Operation Logs: query & filter
// ──────────────────────────────────────────
describe('H10: Operation Logs — query lifecycle', () => {
  it('has module filter in FilterBar slot', () => {
    expect(operationLogsVue).toMatch(/module|模块/)
  })

  it('has handlePageChange', () => {
    expect(operationLogsVue).toContain('handlePageChange')
  })

  it('has handleReset', () => {
    expect(operationLogsVue).toContain('handleReset')
  })

  it('imports getOperationLogs from API', () => {
    expect(operationLogsVue).toMatch(/getOperationLogs/)
  })
})

// ──────────────────────────────────────────
// 5. Operation Logs: result column with tag
// ──────────────────────────────────────────
describe('H10: Operation Logs — result display', () => {
  it('shows result column with tag for success/failure', () => {
    expect(operationLogsVue).toMatch(/el-tag/)
  })

  it('shows error message with overflow tooltip', () => {
    expect(operationLogsVue).toMatch(/errorMessage.*show-overflow-tooltip|show-overflow-tooltip.*errorMessage/)
  })
})
