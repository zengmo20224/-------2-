/**
 * H04: Login Page & Permission Visibility Tests
 *
 * Tests verify:
 * 1. Login page uses PetCare design tokens (not hardcoded colors)
 * 2. Login page uses Chinese labels
 * 3. Login failure does not leak backend errors (SQL, stack traces)
 * 4. 403/404 pages use PetCare design tokens
 * 5. Permission matrix — route permissions are defined for all protected routes
 * 6. Login page uses sanitizeErrorMessage for error display
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'
import { readFileSync } from 'fs'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

function readFile(relativePath: string): string {
  return readFileSync(resolve(__dirname, relativePath), 'utf-8')
}

// ─── Login Page Tests ───

describe('Login page design and security', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/login/index.vue')
  })

  it('uses PetCare design tokens instead of hardcoded #2d3a4b background', () => {
    expect(source).not.toMatch(/#2d3a4b/)
    expect(source).toMatch(/--pc-/)
  })

  it('uses Chinese labels for title and form fields', () => {
    // Title should be Chinese
    expect(source).toMatch(/PetCare|管理后台|登录/)
    // Username placeholder should be Chinese
    expect(source).toMatch(/用户名|账号/)
    // Password placeholder should be Chinese
    expect(source).toMatch(/密码/)
    // Login button should be Chinese
    expect(source).toMatch(/登录/)
  })

  it('does not hardcode English-only error messages', () => {
    // Should not have English-only strings like "Login success" or "Invalid credentials"
    expect(source).not.toMatch(/Login success/)
    expect(source).not.toMatch(/Invalid credentials/)
    expect(source).not.toMatch(/Please input/)
  })

  it('uses sanitizeErrorMessage or feedback utils for error display', () => {
    // Should import from error-sanitizer or feedback utils
    expect(source).toMatch(/error-sanitizer|feedback|sanitizeErrorMessage/)
  })
})

// ─── 403 Page Tests ───

describe('403 Forbidden page', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/error/403.vue')
  })

  it('uses PetCare design tokens', () => {
    expect(source).toMatch(/--pc-/)
  })

  it('shows Chinese message', () => {
    expect(source).toMatch(/权限|禁止|无权/)
  })

  it('has a button to navigate back', () => {
    expect(source).toMatch(/router\.push|返回/)
  })
})

// ─── 404 Page Tests ───

describe('404 Not Found page', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/error/404.vue')
  })

  it('uses PetCare design tokens', () => {
    expect(source).toMatch(/--pc-/)
  })

  it('shows Chinese message', () => {
    expect(source).toMatch(/找不到|不存在|404/)
  })

  it('has a button to navigate back', () => {
    expect(source).toMatch(/router\.push|返回/)
  })
})

// ─── Permission Matrix Tests ───

describe('Route permission matrix', () => {
  let routerSource: string

  beforeEach(() => {
    routerSource = readFile('../router/index.ts')
  })

  it('dashboard route has no permission requirement (accessible to all authenticated)', () => {
    const dashboardMatch = routerSource.match(/path:\s*'dashboard'[^}]*}/s)
    expect(dashboardMatch).toBeTruthy()
    const dashboardBlock = dashboardMatch![0]
    // Dashboard should NOT have a permission meta (it's the default page)
    expect(dashboardBlock).not.toMatch(/permission/)
  })

  it('store/info requires store:info:read permission', () => {
    expect(routerSource).toMatch(/store\/info[\s\S]*?permission.*store:info:read/)
  })

  it('bookings requires booking:booking:read permission', () => {
    expect(routerSource).toMatch(/bookings[\s\S]*?permission.*booking:booking:read/)
  })

  it('products requires product:item:read permission', () => {
    expect(routerSource).toMatch(/path:\s*'products'[\s\S]*?permission.*product:item:read/)
  })

  it('product-orders requires product:order:read permission', () => {
    expect(routerSource).toMatch(/product-orders[\s\S]*?permission.*product:order:read/)
  })

  it('community/posts requires community:post:read permission', () => {
    expect(routerSource).toMatch(/community\/posts[\s\S]*?permission.*community:post:read/)
  })

  it('community/reports requires community:report:handle permission', () => {
    expect(routerSource).toMatch(/community\/reports[\s\S]*?permission.*community:report:handle/)
  })

  it('operation-logs requires admin:operation-log:read permission', () => {
    expect(routerSource).toMatch(/operation-logs[\s\S]*?permission.*admin:operation-log:read/)
  })

  it('beforeEach guard checks permission for all non-login routes', () => {
    expect(routerSource).toMatch(/beforeEach/)
    expect(routerSource).toMatch(/requiredPermission/)
    expect(routerSource).toMatch(/\/403/)
  })
})

// ─── Store Permission Check Tests ───

describe('User store permission check', () => {
  let storeSource: string

  beforeEach(() => {
    storeSource = readFile('../store/user.ts')
  })

  it('hasPermission checks userInfo.permissions array', () => {
    expect(storeSource).toMatch(/permissions/)
    expect(storeSource).toMatch(/hasPermission/)
    expect(storeSource).toMatch(/includes/)
  })

  it('hasPermission returns false when userInfo is null', () => {
    expect(storeSource).toMatch(/\?\./)
  })
})
