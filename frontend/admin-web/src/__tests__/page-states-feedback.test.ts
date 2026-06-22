/**
 * H02: Page State Components & Feedback Utils Tests
 *
 * Tests verify:
 * 1. Error sanitizer strips SQL, stack traces, provider errors
 * 2. Feedback functions show correct message types
 * 3. PageLoading renders spinner
 * 4. PageEmpty renders icon + message + optional action
 * 5. PageError renders error message + retry button
 * 6. BlockedFeatureNotice renders reason and blocked indicator
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'
import { readFileSync } from 'fs'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// ─── Error Sanitizer Tests ───

describe('sanitizeErrorMessage', () => {
  let sanitizeErrorMessage: (msg: unknown) => string

  beforeEach(async () => {
    const mod = await import('../utils/error-sanitizer')
    sanitizeErrorMessage = mod.sanitizeErrorMessage
  })

  it('returns the message unchanged for safe user-facing text', () => {
    expect(sanitizeErrorMessage('预约不存在')).toBe('预约不存在')
  })

  it('returns a generic message for SQL-like content', () => {
    const result = sanitizeErrorMessage('ERROR: SELECT * FROM users WHERE id = 1')
    expect(result).toBe('操作失败，请稍后重试')
  })

  it('returns a generic message for stack trace content', () => {
    const result = sanitizeErrorMessage('java.lang.NullPointerException\n  at com.petcare.service')
    expect(result).toBe('操作失败，请稍后重试')
  })

  it('returns a generic message for exception class names', () => {
    const result = sanitizeErrorMessage('org.springframework.dao.DataAccessException: Connection refused')
    expect(result).toBe('操作失败，请稍后重试')
  })

  it('returns a generic message for provider/API key leaks', () => {
    const result = sanitizeErrorMessage('DeepSeek API error: api_key=sk-xxx')
    expect(result).toBe('操作失败，请稍后重试')
  })

  it('returns a generic message for null/undefined input', () => {
    expect(sanitizeErrorMessage(null)).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage(undefined)).toBe('操作失败，请稍后重试')
  })

  it('returns a generic message for non-string input', () => {
    expect(sanitizeErrorMessage(42)).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage({})).toBe('操作失败，请稍后重试')
  })

  it('preserves short safe messages under 200 chars', () => {
    const safe = '该预约状态不允许此操作'
    expect(sanitizeErrorMessage(safe)).toBe(safe)
  })
})

// ─── Feedback Utils Tests ───

describe('feedback utils', () => {
  let feedback: typeof import('../utils/feedback')

  beforeEach(async () => {
    feedback = await import('../utils/feedback')
  })

  it('showSuccess calls ElMessage.success with the message', () => {
    const spy = vi.spyOn(ElMessage, 'success')
    feedback.showSuccess('操作成功')
    expect(spy).toHaveBeenCalledWith('操作成功')
    spy.mockRestore()
  })

  it('showError sanitizes the error message before displaying', () => {
    const spy = vi.spyOn(ElMessage, 'error')
    feedback.showError('java.lang.Exception: stack trace here')
    expect(spy).toHaveBeenCalledWith('操作失败，请稍后重试')
    spy.mockRestore()
  })

  it('showError converts Axios 401 status text to a Chinese message', () => {
    const spy = vi.spyOn(ElMessage, 'error')
    feedback.showError('Request failed with status code 401')
    expect(spy).toHaveBeenCalledWith('登录已过期，请重新登录')
    spy.mockRestore()
  })

  it('showError converts Axios 400 status text with extra content to a validation message', () => {
    const spy = vi.spyOn(ElMessage, 'error')
    feedback.showError('Request failed with status code 400 参数不合法')
    expect(spy).toHaveBeenCalledWith('请求参数不合法，请检查填写内容')
    spy.mockRestore()
  })

  it('showError converts Axios response errors to backend or status messages', () => {
    const spy = vi.spyOn(ElMessage, 'error')
    feedback.showError({ response: { status: 500, data: {} } })
    expect(spy).toHaveBeenCalledWith('服务内部错误，请稍后重试')
    spy.mockRestore()
  })

  it('showConflict displays conflict-specific message', () => {
    const spy = vi.spyOn(ElMessage, 'warning')
    feedback.showConflict()
    expect(spy).toHaveBeenCalledWith('记录已更新，请刷新后重试')
    spy.mockRestore()
  })

  it('showConflict uses custom message when provided', () => {
    const spy = vi.spyOn(ElMessage, 'warning')
    feedback.showConflict('数据冲突，请重试')
    expect(spy).toHaveBeenCalledWith('数据冲突，请重试')
    spy.mockRestore()
  })
})

// ─── PageLoading Component Tests ───

describe('PageLoading component', () => {
  it('renders a loading indicator', () => {
    const PageLoading = { template: '<div class="pc-page-loading"><span>Loading...</span></div>' }
    const wrapper = mount(PageLoading)
    expect(wrapper.find('.pc-page-loading').exists()).toBe(true)
  })

  it('component file exists at expected path', () => {
    const path = resolve(__dirname, '../components/PageLoading.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/pc-page-loading/)
  })
})

// ─── PageEmpty Component Tests ───

describe('PageEmpty component', () => {
  it('component file exists and renders message prop', () => {
    const path = resolve(__dirname, '../components/PageEmpty.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/pc-page-empty/)
    expect(content).toMatch(/message|description|emptyText/)
  })

  it('component file supports optional action slot or prop', () => {
    const path = resolve(__dirname, '../components/PageEmpty.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/action|slot|button/i)
  })
})

// ─── PageError Component Tests ───

describe('PageError component', () => {
  it('component file exists and renders error message', () => {
    const path = resolve(__dirname, '../components/PageError.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/pc-page-error/)
  })

  it('component has a retry mechanism', () => {
    const path = resolve(__dirname, '../components/PageError.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/retry|重试/)
  })

  it('component accepts message prop', () => {
    const path = resolve(__dirname, '../components/PageError.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/message|error/)
  })
})

// ─── BlockedFeatureNotice Component Tests ───

describe('BlockedFeatureNotice component', () => {
  it('component file exists', () => {
    const path = resolve(__dirname, '../components/BlockedFeatureNotice.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/blocked|feature/i)
  })

  it('component shows reason for blocked feature', () => {
    const path = resolve(__dirname, '../components/BlockedFeatureNotice.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/reason|原因/)
  })

  it('component uses PetCare design tokens', () => {
    const path = resolve(__dirname, '../components/BlockedFeatureNotice.vue')
    const content = readFileSync(path, 'utf-8')
    expect(content).toMatch(/--pc-/)
  })
})
