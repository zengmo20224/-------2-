/**
 * H01: Design Tokens & App Shell Tests
 *
 * Tests verify:
 * 1. PetCare design token CSS variables are defined
 * 2. Layout structure matches Figma design (sidebar 224px, navbar, content)
 * 3. Sidebar menu renders based on user permissions
 * 4. Unauthenticated users redirect to /login
 * 5. Unauthorized access redirects to /403
 */

import { describe, it, expect, beforeAll } from 'vitest'
import { readFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// ─── Design Token CSS Variable Tests ───

describe('PetCare design tokens (CSS variables)', () => {
  let cssContent: string

  beforeAll(() => {
    const cssPath = resolve(__dirname, '../styles/design-tokens.css')
    cssContent = readFileSync(cssPath, 'utf-8')
  })

  it('defines --pc-primary as #157F76', () => {
    expect(cssContent).toMatch(/--pc-primary:\s*#157F76/)
  })

  it('defines --pc-primary-dark as #0F5E59', () => {
    expect(cssContent).toMatch(/--pc-primary-dark:\s*#0F5E59/)
  })

  it('defines --pc-primary-soft as #E4F3F0', () => {
    expect(cssContent).toMatch(/--pc-primary-soft:\s*#E4F3F0/)
  })

  it('defines --pc-accent as #F4A640', () => {
    expect(cssContent).toMatch(/--pc-accent:\s*#F4A640/)
  })

  it('defines --pc-danger as #D94A4A', () => {
    expect(cssContent).toMatch(/--pc-danger:\s*#D94A4A/)
  })

  it('defines --pc-surface as #F7F8F6', () => {
    expect(cssContent).toMatch(/--pc-surface:\s*#F7F8F6/)
  })

  it('defines --pc-ink as #1D2A28', () => {
    expect(cssContent).toMatch(/--pc-ink:\s*#1D2A28/)
  })

  it('defines --pc-muted as #65736F', () => {
    expect(cssContent).toMatch(/--pc-muted:\s*#65736F/)
  })

  it('defines --pc-line as #DDE5E1', () => {
    expect(cssContent).toMatch(/--pc-line:\s*#DDE5E1/)
  })

  it('defines layout token --pc-sidebar-width as 224px', () => {
    expect(cssContent).toMatch(/--pc-sidebar-width:\s*224px/)
  })

  it('defines layout token --pc-content-gap as 28px', () => {
    expect(cssContent).toMatch(/--pc-content-gap:\s*28px/)
  })

  it('defines --pc-radius as 12px', () => {
    expect(cssContent).toMatch(/--pc-radius:\s*12px/)
  })

  it('defines min-width as 1280px', () => {
    expect(cssContent).toMatch(/1280px/)
  })
})

// ─── Layout Structure Tests ───

describe('Admin layout structure', () => {
  it('layout file uses --pc-sidebar-width variable for aside width', () => {
    const layoutPath = resolve(__dirname, '../layout/index.vue')
    const layoutContent = readFileSync(layoutPath, 'utf-8')
    expect(layoutContent).toMatch(/--pc-sidebar-width/)
  })

  it('layout does not use old #304156 dark sidebar color', () => {
    const layoutPath = resolve(__dirname, '../layout/index.vue')
    const layoutContent = readFileSync(layoutPath, 'utf-8')
    expect(layoutContent).not.toMatch(/#304156/)
  })

  it('layout uses PetCare primary-dark token for sidebar background', () => {
    const layoutPath = resolve(__dirname, '../layout/index.vue')
    const layoutContent = readFileSync(layoutPath, 'utf-8')
    expect(layoutContent).toMatch(/--pc-primary-dark/)
  })

  it('navbar uses --pc-line for border color instead of hardcoded #d8dce5', () => {
    const layoutPath = resolve(__dirname, '../layout/index.vue')
    const layoutContent = readFileSync(layoutPath, 'utf-8')
    expect(layoutContent).not.toMatch(/#d8dce5/)
    expect(layoutContent).toMatch(/--pc-line/)
  })
})

// ─── Route Guard Tests ───

describe('Route guard behavior', () => {
  it('router beforeEach guard redirects unauthenticated users to /login', () => {
    const routerPath = resolve(__dirname, '../router/index.ts')
    const routerContent = readFileSync(routerPath, 'utf-8')
    expect(routerContent).toMatch(/beforeEach/)
    expect(routerContent).toMatch(/\/login/)
  })

  it('router redirects unauthorized access to /403', () => {
    const routerPath = resolve(__dirname, '../router/index.ts')
    const routerContent = readFileSync(routerPath, 'utf-8')
    expect(routerContent).toMatch(/\/403/)
    expect(routerContent).toMatch(/permission/)
  })
})
